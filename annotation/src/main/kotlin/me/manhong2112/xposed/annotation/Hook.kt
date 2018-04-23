package me.manhong2112.xposed.annotation


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class HookAfter(val method: String, val argsType: Array<String> = [], val priority: Int = 50)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class HookPackage(val packageName: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class HookClass(val className: String)
