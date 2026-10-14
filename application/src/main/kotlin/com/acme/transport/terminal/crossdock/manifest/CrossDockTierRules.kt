package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.kit.text.normalize

/** A rule a cross dock tier must pass. Check returns its problems, or none. */
interface CrossDockTierRule {
    fun check(item: CrossDockTier): MutableList<String>
}

data class CrossDockTierRequiredNameRule(var message: String) : CrossDockTierRule {
    override fun check(item: CrossDockTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTierCodePrefixRule(var prefix: String) : CrossDockTierRule {
    override fun check(item: CrossDockTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTierDriverIdLengthRule(var maxLength: Int) : CrossDockTierRule {
    override fun check(item: CrossDockTier): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTierFloorValueRangeRule(var min: Double, var max: Double) : CrossDockTierRule {
    override fun check(item: CrossDockTier): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTierAllowedNameRule(var allowed: MutableList<String>) : CrossDockTierRule {
    override fun check(item: CrossDockTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTierMinimumRangeRule(var min: Double, var max: Double) : CrossDockTierRule {
    override fun check(item: CrossDockTier): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a cross dock tier. */
data class CrossDockTierValidator(var rules: MutableList<CrossDockTierRule>) {
    fun validate(item: CrossDockTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockTier>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockTierValidator(): CrossDockTierValidator {
    return CrossDockTierValidator(
        mutableListOf<CrossDockTierRule>(
            CrossDockTierRequiredNameRule("cross dock tier needs a value"),
            CrossDockTierCodePrefixRule("ACM"),
            CrossDockTierDriverIdLengthRule(16),
            CrossDockTierFloorValueRangeRule(9.4, 2036.3),
            CrossDockTierAllowedNameRule(mutableListOf<String>("adaptive")),
            CrossDockTierMinimumRangeRule(2.8, 3220.3),
        ),
    )
}
