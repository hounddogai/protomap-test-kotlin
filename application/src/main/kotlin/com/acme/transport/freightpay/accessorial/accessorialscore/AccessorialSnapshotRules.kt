package com.acme.transport.freightpay.accessorial.accessorialscore

/** A rule a accessorial snapshot must pass. Check returns its problems, or none. */
interface AccessorialSnapshotRule {
    fun check(item: AccessorialSnapshot): MutableList<String>
}

data class AccessorialSnapshotUnitLengthRule(var maxLength: Int) : AccessorialSnapshotRule {
    override fun check(item: AccessorialSnapshot): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialSnapshotRequiredCodeRule(var message: String) : AccessorialSnapshotRule {
    override fun check(item: AccessorialSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialSnapshotDurationMinutesRangeRule(var min: Int, var max: Int) : AccessorialSnapshotRule {
    override fun check(item: AccessorialSnapshot): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialSnapshotTitleLengthRule(var maxLength: Int) : AccessorialSnapshotRule {
    override fun check(item: AccessorialSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialSnapshotVisibleGuardRule(var limit: Int) : AccessorialSnapshotRule {
    override fun check(item: AccessorialSnapshot): MutableList<String> {
        if (item.visible && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialSnapshotRequiredTitleRule(var message: String) : AccessorialSnapshotRule {
    override fun check(item: AccessorialSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial snapshot. */
data class AccessorialSnapshotValidator(var rules: MutableList<AccessorialSnapshotRule>) {
    fun validate(item: AccessorialSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialSnapshotValidator(): AccessorialSnapshotValidator {
    return AccessorialSnapshotValidator(
        mutableListOf<AccessorialSnapshotRule>(
            AccessorialSnapshotUnitLengthRule(1000),
            AccessorialSnapshotRequiredCodeRule("accessorial snapshot needs this field"),
            AccessorialSnapshotDurationMinutesRangeRule(80, 80),
            AccessorialSnapshotTitleLengthRule(80),
            AccessorialSnapshotVisibleGuardRule(3600),
            AccessorialSnapshotRequiredTitleRule("accessorial snapshot needs an entry"),
        ),
    )
}
