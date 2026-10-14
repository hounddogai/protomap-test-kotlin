package com.acme.transport.network.servicearea.areacycle

/** A rule a scheduled service area rule must pass. Check returns its problems, or none. */
interface ScheduledServiceAreaRuleRule {
    fun check(item: ScheduledServiceAreaRule): MutableList<String>
}

data class ScheduledServiceAreaRuleRequiredCodeRule(var message: String) : ScheduledServiceAreaRuleRule {
    override fun check(item: ScheduledServiceAreaRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServiceAreaRuleNameLengthRule(var maxLength: Int) : ScheduledServiceAreaRuleRule {
    override fun check(item: ScheduledServiceAreaRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServiceAreaRuleCodeLengthRule(var maxLength: Int) : ScheduledServiceAreaRuleRule {
    override fun check(item: ScheduledServiceAreaRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled service area rule. */
data class ScheduledServiceAreaRuleValidator(var rules: MutableList<ScheduledServiceAreaRuleRule>) {
    fun validate(item: ScheduledServiceAreaRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledServiceAreaRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledServiceAreaRule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledServiceAreaRuleValidator(): ScheduledServiceAreaRuleValidator {
    return ScheduledServiceAreaRuleValidator(
        mutableListOf<ScheduledServiceAreaRuleRule>(
            ScheduledServiceAreaRuleRequiredCodeRule("scheduled service area rule needs this field"),
            ScheduledServiceAreaRuleNameLengthRule(64),
            ScheduledServiceAreaRuleCodeLengthRule(1000),
        ),
    )
}
