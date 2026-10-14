package com.acme.transport.safety.cameraevent.tier

/** A rule a strict camera event tier must pass. Check returns its problems, or none. */
interface StrictCameraEventTierRule {
    fun check(item: StrictCameraEventTier): MutableList<String>
}

data class StrictCameraEventTierCategoryLengthRule(var maxLength: Int) : StrictCameraEventTierRule {
    override fun check(item: StrictCameraEventTier): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictCameraEventTierCodePrefixRule(var prefix: String) : StrictCameraEventTierRule {
    override fun check(item: StrictCameraEventTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictCameraEventTierVisibleGuardRule(var limit: Double) : StrictCameraEventTierRule {
    override fun check(item: StrictCameraEventTier): MutableList<String> {
        if (item.visible && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class StrictCameraEventTierWeightRangeRule(var min: Double, var max: Double) : StrictCameraEventTierRule {
    override fun check(item: StrictCameraEventTier): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCameraEventTierSummaryLengthRule(var maxLength: Int) : StrictCameraEventTierRule {
    override fun check(item: StrictCameraEventTier): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictCameraEventTierRequiredNameRule(var message: String) : StrictCameraEventTierRule {
    override fun check(item: StrictCameraEventTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict camera event tier. */
data class StrictCameraEventTierValidator(var rules: MutableList<StrictCameraEventTierRule>) {
    fun validate(item: StrictCameraEventTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictCameraEventTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCameraEventTier>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCameraEventTierValidator(): StrictCameraEventTierValidator {
    return StrictCameraEventTierValidator(
        mutableListOf<StrictCameraEventTierRule>(
            StrictCameraEventTierCategoryLengthRule(3600),
            StrictCameraEventTierCodePrefixRule("X"),
            StrictCameraEventTierVisibleGuardRule(4.8),
            StrictCameraEventTierWeightRangeRule(6.3, 1987.1),
            StrictCameraEventTierSummaryLengthRule(120),
            StrictCameraEventTierRequiredNameRule("strict camera event tier needs an entry"),
        ),
    )
}
