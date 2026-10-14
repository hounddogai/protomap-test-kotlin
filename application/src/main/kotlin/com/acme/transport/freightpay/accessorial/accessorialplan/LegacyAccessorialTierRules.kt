package com.acme.transport.freightpay.accessorial.accessorialplan

/** A rule a legacy accessorial tier must pass. Check returns its problems, or none. */
interface LegacyAccessorialTierRule {
    fun check(item: LegacyAccessorialTier): MutableList<String>
}

data class LegacyAccessorialTierDriverIdLengthRule(var maxLength: Int) : LegacyAccessorialTierRule {
    override fun check(item: LegacyAccessorialTier): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAccessorialTierCodePrefixRule(var prefix: String) : LegacyAccessorialTierRule {
    override fun check(item: LegacyAccessorialTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAccessorialTierRequiredTitleRule(var message: String) : LegacyAccessorialTierRule {
    override fun check(item: LegacyAccessorialTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAccessorialTierRequiredCodeRule(var message: String) : LegacyAccessorialTierRule {
    override fun check(item: LegacyAccessorialTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAccessorialTierBudgetRangeRule(var min: Double, var max: Double) : LegacyAccessorialTierRule {
    override fun check(item: LegacyAccessorialTier): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy accessorial tier. */
data class LegacyAccessorialTierValidator(var rules: MutableList<LegacyAccessorialTierRule>) {
    fun validate(item: LegacyAccessorialTier): MutableList<String> {
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

    fun requireValid(item: LegacyAccessorialTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAccessorialTier>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAccessorialTierValidator(): LegacyAccessorialTierValidator {
    return LegacyAccessorialTierValidator(
        mutableListOf<LegacyAccessorialTierRule>(
            LegacyAccessorialTierDriverIdLengthRule(120),
            LegacyAccessorialTierCodePrefixRule("REF"),
            LegacyAccessorialTierRequiredTitleRule("legacy accessorial tier needs an entry"),
            LegacyAccessorialTierRequiredCodeRule("legacy accessorial tier needs this field"),
            LegacyAccessorialTierBudgetRangeRule(6.5, 3847.4),
        ),
    )
}
