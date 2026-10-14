package com.acme.transport.fleetops.serviceplan.planpreference

/** A rule a service plan rule must pass. Check returns its problems, or none. */
interface ServicePlanRuleRule {
    fun check(item: ServicePlanRule): MutableList<String>
}

data class ServicePlanRuleEndsAtRangeRule(var min: Int, var max: Int) : ServicePlanRuleRule {
    override fun check(item: ServicePlanRule): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRuleLabelLengthRule(var maxLength: Int) : ServicePlanRuleRule {
    override fun check(item: ServicePlanRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRuleCodeLengthRule(var maxLength: Int) : ServicePlanRuleRule {
    override fun check(item: ServicePlanRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan rule. */
data class ServicePlanRuleValidator(var rules: MutableList<ServicePlanRuleRule>) {
    fun validate(item: ServicePlanRule): MutableList<String> {
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

    fun requireValid(item: ServicePlanRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanRule>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanRuleValidator(): ServicePlanRuleValidator {
    return ServicePlanRuleValidator(
        mutableListOf<ServicePlanRuleRule>(
            ServicePlanRuleEndsAtRangeRule(64, 1000),
            ServicePlanRuleLabelLengthRule(255),
            ServicePlanRuleCodeLengthRule(3600),
        ),
    )
}
