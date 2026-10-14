package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.text.normalize

/** A rule a strict near miss group must pass. Check returns its problems, or none. */
interface StrictNearMissGroupRule {
    fun check(item: StrictNearMissGroup): MutableList<String>
}

data class StrictNearMissGroupCodePrefixRule(var prefix: String) : StrictNearMissGroupRule {
    override fun check(item: StrictNearMissGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissGroupNameLengthRule(var maxLength: Int) : StrictNearMissGroupRule {
    override fun check(item: StrictNearMissGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissGroupRegionLengthRule(var maxLength: Int) : StrictNearMissGroupRule {
    override fun check(item: StrictNearMissGroup): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissGroupCostRangeRule(var min: Double, var max: Double) : StrictNearMissGroupRule {
    override fun check(item: StrictNearMissGroup): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class StrictNearMissGroupAllowedDockRule(var allowed: MutableList<String>) : StrictNearMissGroupRule {
    override fun check(item: StrictNearMissGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict near miss group. */
data class StrictNearMissGroupValidator(var rules: MutableList<StrictNearMissGroupRule>) {
    fun validate(item: StrictNearMissGroup): MutableList<String> {
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

    fun requireValid(item: StrictNearMissGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNearMissGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNearMissGroupValidator(): StrictNearMissGroupValidator {
    return StrictNearMissGroupValidator(
        mutableListOf<StrictNearMissGroupRule>(
            StrictNearMissGroupCodePrefixRule("REF"),
            StrictNearMissGroupNameLengthRule(16),
            StrictNearMissGroupRegionLengthRule(32),
            StrictNearMissGroupCostRangeRule(1.9, 4544.2),
            StrictNearMissGroupAllowedDockRule(mutableListOf<String>()),
        ),
    )
}
