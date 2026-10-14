package com.acme.transport.safety.cameraevent.eventaudit

/** A rule a manual camera event bundle must pass. Check returns its problems, or none. */
interface ManualCameraEventBundleRule {
    fun check(item: ManualCameraEventBundle): MutableList<String>
}

data class ManualCameraEventBundleDockLengthRule(var maxLength: Int) : ManualCameraEventBundleRule {
    override fun check(item: ManualCameraEventBundle): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventBundleRequiredNameRule(var message: String) : ManualCameraEventBundleRule {
    override fun check(item: ManualCameraEventBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventBundleVisibleGuardRule(var limit: Double) : ManualCameraEventBundleRule {
    override fun check(item: ManualCameraEventBundle): MutableList<String> {
        if (item.visible && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventBundleRequiredCodeRule(var message: String) : ManualCameraEventBundleRule {
    override fun check(item: ManualCameraEventBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventBundleNameLengthRule(var maxLength: Int) : ManualCameraEventBundleRule {
    override fun check(item: ManualCameraEventBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventBundleCodePrefixRule(var prefix: String) : ManualCameraEventBundleRule {
    override fun check(item: ManualCameraEventBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual camera event bundle. */
data class ManualCameraEventBundleValidator(var rules: MutableList<ManualCameraEventBundleRule>) {
    fun validate(item: ManualCameraEventBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: ManualCameraEventBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCameraEventBundle>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultManualCameraEventBundleValidator(): ManualCameraEventBundleValidator {
    return ManualCameraEventBundleValidator(
        mutableListOf<ManualCameraEventBundleRule>(
            ManualCameraEventBundleDockLengthRule(255),
            ManualCameraEventBundleRequiredNameRule("manual camera event bundle needs a value"),
            ManualCameraEventBundleVisibleGuardRule(2.2),
            ManualCameraEventBundleRequiredCodeRule("manual camera event bundle needs a value"),
            ManualCameraEventBundleNameLengthRule(32),
            ManualCameraEventBundleCodePrefixRule("REF"),
        ),
    )
}
