package com.example.ioscc

import android.graphics.Outline
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {

    // ---- تنظیمات ----
    private val tileRadiusDp = 22f      // گردی کاشی‌ها
    private val blurRadiusPx = 90       // شدت بلور پس‌زمینه
    // ------------------

    private val seen = HashSet<String>()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.systemui") return
        XposedBridge.log("IOSCC: loaded in SystemUI")

        XposedHelpers.findAndHookMethod(
            View::class.java, "onAttachedToWindow",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val v = param.thisObject as? View ?: return
                    val name = v.javaClass.name
                    val isCc = name.contains("ControlCenter", true) ||
                            name.contains("ControlPanel", true)
                    val isTile = name.contains("tile", true) &&
                            (isCc || name.contains("QS", true))
                    if (!isCc && !isTile) return

                    // برای پیدا کردن اسم کلاس‌های درست در MIUI 14
                    if (seen.add(name)) XposedBridge.log("IOSCC: candidate $name")

                    try {
                        if (isTile) roundTile(v)
                        if (name.contains("Window", true) && isCc) blurWindow(v)
                    } catch (t: Throwable) {
                        XposedBridge.log("IOSCC: error on $name: $t")
                    }
                }
            })
    }

    private fun roundTile(v: View) {
        val r = tileRadiusDp * v.resources.displayMetrics.density
        v.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, r)
            }
        }
        v.clipToOutline = true
    }

    private fun blurWindow(v: View) {
        v.post {
            val root = v.rootView
            val lp = root.layoutParams as? WindowManager.LayoutParams ?: return@post
            lp.blurBehindRadius = blurRadiusPx
            @Suppress("DEPRECATION")
            lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            val wm = root.context.getSystemService(WindowManager::class.java)
            wm.updateViewLayout(root, lp)
        }
    }
}
