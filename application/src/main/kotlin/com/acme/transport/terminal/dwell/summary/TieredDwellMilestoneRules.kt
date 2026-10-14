package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.text.normalize

/** A rule a tiered dwell milestone must pass. Check returns its problems, or none. */
interface TieredDwellMilestoneRule {
    fun check(item: TieredDwellMilestone): MutableList<String>
}

data class TieredDwellMilestoneRequiredLocaleRule(var message: String) : TieredDwellMilestoneRule {
    override fun check(item: TieredDwellMilestone): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDwellMilestoneCountryLengthRule(var maxLength: Int) : TieredDwellMilestoneRule {
    override fun check(item: TieredDwellMilestone): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDwellMilestoneLocaleLengthRule(var maxLength: Int) : TieredDwellMilestoneRule {
    override fun check(item: TieredDwellMilestone): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDwellMilestoneAllowedCodeRule(var allowed: MutableList<String>) : TieredDwellMilestoneRule {
    override fun check(item: TieredDwellMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered dwell milestone. */
data class TieredDwellMilestoneValidator(var rules: MutableList<TieredDwellMilestoneRule>) {
    fun validate(item: TieredDwellMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDwellMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDwellMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDwellMilestoneValidator(): TieredDwellMilestoneValidator {
    return TieredDwellMilestoneValidator(
        mutableListOf<TieredDwellMilestoneRule>(
            TieredDwellMilestoneRequiredLocaleRule("tiered dwell milestone needs a value"),
            TieredDwellMilestoneCountryLengthRule(16),
            TieredDwellMilestoneLocaleLengthRule(16),
            TieredDwellMilestoneAllowedCodeRule(mutableListOf<String>("rolling")),
        ),
    )
}
