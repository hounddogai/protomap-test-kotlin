package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.text.normalize

/** A rule a rolling node tier must pass. Check returns its problems, or none. */
interface RollingNodeTierRule {
    fun check(item: RollingNodeTier): MutableList<String>
}

data class RollingNodeTierCodePrefixRule(var prefix: String) : RollingNodeTierRule {
    override fun check(item: RollingNodeTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeTierAllowedNameRule(var allowed: MutableList<String>) : RollingNodeTierRule {
    override fun check(item: RollingNodeTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeTierRequiredNameRule(var message: String) : RollingNodeTierRule {
    override fun check(item: RollingNodeTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeTierAttemptsRangeRule(var min: Int, var max: Int) : RollingNodeTierRule {
    override fun check(item: RollingNodeTier): MutableList<String> {
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

/** Runs every rule of a rolling node tier. */
data class RollingNodeTierValidator(var rules: MutableList<RollingNodeTierRule>) {
    fun validate(item: RollingNodeTier): MutableList<String> {
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

    fun requireValid(item: RollingNodeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingNodeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingNodeTierValidator(): RollingNodeTierValidator {
    return RollingNodeTierValidator(
        mutableListOf<RollingNodeTierRule>(
            RollingNodeTierCodePrefixRule("ACM"),
            RollingNodeTierAllowedNameRule(mutableListOf<String>("lenient")),
            RollingNodeTierRequiredNameRule("rolling node tier needs a value"),
            RollingNodeTierAttemptsRangeRule(120, 255),
        ),
    )
}
