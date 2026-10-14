package com.acme.transport.linehaul.relay.bundle

/** A rule a global relay bundle must pass. Check returns its problems, or none. */
interface GlobalRelayBundleRule {
    fun check(item: GlobalRelayBundle): MutableList<String>
}

data class GlobalRelayBundleLabelLengthRule(var maxLength: Int) : GlobalRelayBundleRule {
    override fun check(item: GlobalRelayBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRelayBundleDescriptionLengthRule(var maxLength: Int) : GlobalRelayBundleRule {
    override fun check(item: GlobalRelayBundle): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRelayBundleExpiresAtRangeRule(var min: Int, var max: Int) : GlobalRelayBundleRule {
    override fun check(item: GlobalRelayBundle): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRelayBundleVisibleGuardRule(var limit: Double) : GlobalRelayBundleRule {
    override fun check(item: GlobalRelayBundle): MutableList<String> {
        if (item.visible && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global relay bundle. */
data class GlobalRelayBundleValidator(var rules: MutableList<GlobalRelayBundleRule>) {
    fun validate(item: GlobalRelayBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRelayBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRelayBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRelayBundleValidator(): GlobalRelayBundleValidator {
    return GlobalRelayBundleValidator(
        mutableListOf<GlobalRelayBundleRule>(
            GlobalRelayBundleLabelLengthRule(1000),
            GlobalRelayBundleDescriptionLengthRule(1000),
            GlobalRelayBundleExpiresAtRangeRule(255, 3600),
            GlobalRelayBundleVisibleGuardRule(9.2),
        ),
    )
}
