package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.text.normalize

/** A rule a utilization tier must pass. Check returns its problems, or none. */
interface UtilizationTierRule {
    fun check(item: UtilizationTier): MutableList<String>
}

data class UtilizationTierAllowedTimezoneRule(var allowed: MutableList<String>) : UtilizationTierRule {
    override fun check(item: UtilizationTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationTierLabelLengthRule(var maxLength: Int) : UtilizationTierRule {
    override fun check(item: UtilizationTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationTierRequiredTimezoneRule(var message: String) : UtilizationTierRule {
    override fun check(item: UtilizationTier): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationTierTimezoneLengthRule(var maxLength: Int) : UtilizationTierRule {
    override fun check(item: UtilizationTier): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationTierRateRangeRule(var min: Double, var max: Double) : UtilizationTierRule {
    override fun check(item: UtilizationTier): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class UtilizationTierRequiredCodeRule(var message: String) : UtilizationTierRule {
    override fun check(item: UtilizationTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization tier. */
data class UtilizationTierValidator(var rules: MutableList<UtilizationTierRule>) {
    fun validate(item: UtilizationTier): MutableList<String> {
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

    fun requireValid(item: UtilizationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationTierValidator(): UtilizationTierValidator {
    return UtilizationTierValidator(
        mutableListOf<UtilizationTierRule>(
            UtilizationTierAllowedTimezoneRule(mutableListOf<String>("weighted", "pending", "manual")),
            UtilizationTierLabelLengthRule(1000),
            UtilizationTierRequiredTimezoneRule("utilization tier needs a value"),
            UtilizationTierTimezoneLengthRule(32),
            UtilizationTierRateRangeRule(5.3, 1766.2),
            UtilizationTierRequiredCodeRule("utilization tier needs this field"),
        ),
    )
}
