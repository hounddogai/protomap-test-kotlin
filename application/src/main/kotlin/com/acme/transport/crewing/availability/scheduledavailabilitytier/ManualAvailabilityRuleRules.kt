package com.acme.transport.crewing.availability.scheduledavailabilitytier

/** A rule a manual availability rule must pass. Check returns its problems, or none. */
interface ManualAvailabilityRuleRule {
    fun check(item: ManualAvailabilityRule): MutableList<String>
}

data class ManualAvailabilityRuleRequiredReviewerRule(var message: String) : ManualAvailabilityRuleRule {
    override fun check(item: ManualAvailabilityRule): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilityRuleStopCountRangeRule(var min: Int, var max: Int) : ManualAvailabilityRuleRule {
    override fun check(item: ManualAvailabilityRule): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class ManualAvailabilityRuleCodeLengthRule(var maxLength: Int) : ManualAvailabilityRuleRule {
    override fun check(item: ManualAvailabilityRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual availability rule. */
data class ManualAvailabilityRuleValidator(var rules: MutableList<ManualAvailabilityRuleRule>) {
    fun validate(item: ManualAvailabilityRule): MutableList<String> {
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

    fun requireValid(item: ManualAvailabilityRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAvailabilityRule>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAvailabilityRuleValidator(): ManualAvailabilityRuleValidator {
    return ManualAvailabilityRuleValidator(
        mutableListOf<ManualAvailabilityRuleRule>(
            ManualAvailabilityRuleRequiredReviewerRule("manual availability rule needs this field"),
            ManualAvailabilityRuleStopCountRangeRule(1000, 3600),
            ManualAvailabilityRuleCodeLengthRule(32),
        ),
    )
}
