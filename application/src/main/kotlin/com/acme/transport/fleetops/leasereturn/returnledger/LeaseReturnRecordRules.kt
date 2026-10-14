package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.normalize

/** A rule a lease return record must pass. Check returns its problems, or none. */
interface LeaseReturnRecordRule {
    fun check(item: LeaseReturnRecord): MutableList<String>
}

data class LeaseReturnRecordBudgetRangeRule(var min: Double, var max: Double) : LeaseReturnRecordRule {
    override fun check(item: LeaseReturnRecord): MutableList<String> {
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

data class LeaseReturnRecordAllowedTitleRule(var allowed: MutableList<String>) : LeaseReturnRecordRule {
    override fun check(item: LeaseReturnRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRecordTitleLengthRule(var maxLength: Int) : LeaseReturnRecordRule {
    override fun check(item: LeaseReturnRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRecordWeightRangeRule(var min: Double, var max: Double) : LeaseReturnRecordRule {
    override fun check(item: LeaseReturnRecord): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRecordCodePrefixRule(var prefix: String) : LeaseReturnRecordRule {
    override fun check(item: LeaseReturnRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return record. */
data class LeaseReturnRecordValidator(var rules: MutableList<LeaseReturnRecordRule>) {
    fun validate(item: LeaseReturnRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnRecordValidator(): LeaseReturnRecordValidator {
    return LeaseReturnRecordValidator(
        mutableListOf<LeaseReturnRecordRule>(
            LeaseReturnRecordBudgetRangeRule(1.0, 395.8),
            LeaseReturnRecordAllowedTitleRule(mutableListOf<String>("global", "capped")),
            LeaseReturnRecordTitleLengthRule(32),
            LeaseReturnRecordWeightRangeRule(1.0, 248.0),
            LeaseReturnRecordCodePrefixRule("ACM"),
        ),
    )
}
