package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.text.normalize

/** A rule a legacy dispute profile must pass. Check returns its problems, or none. */
interface LegacyDisputeProfileRule {
    fun check(item: LegacyDisputeProfile): MutableList<String>
}

data class LegacyDisputeProfileCodePrefixRule(var prefix: String) : LegacyDisputeProfileRule {
    override fun check(item: LegacyDisputeProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDisputeProfileCostRangeRule(var min: Double, var max: Double) : LegacyDisputeProfileRule {
    override fun check(item: LegacyDisputeProfile): MutableList<String> {
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

data class LegacyDisputeProfileThresholdRangeRule(var min: Double, var max: Double) : LegacyDisputeProfileRule {
    override fun check(item: LegacyDisputeProfile): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class LegacyDisputeProfileAllowedCodeRule(var allowed: MutableList<String>) : LegacyDisputeProfileRule {
    override fun check(item: LegacyDisputeProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDisputeProfileCodeLengthRule(var maxLength: Int) : LegacyDisputeProfileRule {
    override fun check(item: LegacyDisputeProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy dispute profile. */
data class LegacyDisputeProfileValidator(var rules: MutableList<LegacyDisputeProfileRule>) {
    fun validate(item: LegacyDisputeProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDisputeProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDisputeProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDisputeProfileValidator(): LegacyDisputeProfileValidator {
    return LegacyDisputeProfileValidator(
        mutableListOf<LegacyDisputeProfileRule>(
            LegacyDisputeProfileCodePrefixRule("X"),
            LegacyDisputeProfileCostRangeRule(4.5, 822.6),
            LegacyDisputeProfileThresholdRangeRule(6.7, 1180.4),
            LegacyDisputeProfileAllowedCodeRule(mutableListOf<String>("scheduled")),
            LegacyDisputeProfileCodeLengthRule(80),
        ),
    )
}
