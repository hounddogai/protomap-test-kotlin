package com.acme.transport.customs.clearance.estimate

/** A rule a regional clearance profile must pass. Check returns its problems, or none. */
interface RegionalClearanceProfileRule {
    fun check(item: RegionalClearanceProfile): MutableList<String>
}

data class RegionalClearanceProfileActiveGuardRule(var limit: Double) : RegionalClearanceProfileRule {
    override fun check(item: RegionalClearanceProfile): MutableList<String> {
        if (item.active && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceProfileRequiredTitleRule(var message: String) : RegionalClearanceProfileRule {
    override fun check(item: RegionalClearanceProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceProfileRequiredCodeRule(var message: String) : RegionalClearanceProfileRule {
    override fun check(item: RegionalClearanceProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceProfileMaximumRangeRule(
    var min: Double,
    var max: Double,
) : RegionalClearanceProfileRule {
    override fun check(item: RegionalClearanceProfile): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class RegionalClearanceProfileCodeLengthRule(var maxLength: Int) : RegionalClearanceProfileRule {
    override fun check(item: RegionalClearanceProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceProfileCodePrefixRule(var prefix: String) : RegionalClearanceProfileRule {
    override fun check(item: RegionalClearanceProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional clearance profile. */
data class RegionalClearanceProfileValidator(var rules: MutableList<RegionalClearanceProfileRule>) {
    fun validate(item: RegionalClearanceProfile): MutableList<String> {
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

    fun requireValid(item: RegionalClearanceProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalClearanceProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalClearanceProfileValidator(): RegionalClearanceProfileValidator {
    return RegionalClearanceProfileValidator(
        mutableListOf<RegionalClearanceProfileRule>(
            RegionalClearanceProfileActiveGuardRule(9.2),
            RegionalClearanceProfileRequiredTitleRule("regional clearance profile needs this field"),
            RegionalClearanceProfileRequiredCodeRule("regional clearance profile needs this field"),
            RegionalClearanceProfileMaximumRangeRule(4.9, 4890.9),
            RegionalClearanceProfileCodeLengthRule(32),
            RegionalClearanceProfileCodePrefixRule("ACM"),
        ),
    )
}
