package com.acme.transport.lastmile.drop.bundle

import com.acme.transport.kit.text.normalize

/** A rule a drop tier must pass. Check returns its problems, or none. */
interface DropTierRule {
    fun check(item: DropTier): MutableList<String>
}

data class DropTierAttemptsRangeRule(var min: Int, var max: Int) : DropTierRule {
    override fun check(item: DropTier): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class DropTierCodePrefixRule(var prefix: String) : DropTierRule {
    override fun check(item: DropTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropTierRequiredCodeRule(var message: String) : DropTierRule {
    override fun check(item: DropTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropTierAllowedLabelRule(var allowed: MutableList<String>) : DropTierRule {
    override fun check(item: DropTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DropTierMinimumRangeRule(var min: Double, var max: Double) : DropTierRule {
    override fun check(item: DropTier): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropTierRequiredTimezoneRule(var message: String) : DropTierRule {
    override fun check(item: DropTier): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop tier. */
data class DropTierValidator(var rules: MutableList<DropTierRule>) {
    fun validate(item: DropTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropTier>): MutableMap<String, MutableList<String>> {
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

fun defaultDropTierValidator(): DropTierValidator {
    return DropTierValidator(
        mutableListOf<DropTierRule>(
            DropTierAttemptsRangeRule(64, 120),
            DropTierCodePrefixRule("ACM"),
            DropTierRequiredCodeRule("drop tier needs a value"),
            DropTierAllowedLabelRule(mutableListOf<String>("seasonal", "global")),
            DropTierMinimumRangeRule(1.4, 1886.7),
            DropTierRequiredTimezoneRule("drop tier needs a value"),
        ),
    )
}
