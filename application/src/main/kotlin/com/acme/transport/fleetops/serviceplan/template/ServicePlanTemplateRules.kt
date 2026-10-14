package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.text.normalize

/** A rule a service plan template must pass. Check returns its problems, or none. */
interface ServicePlanTemplateRule {
    fun check(item: ServicePlanTemplate): MutableList<String>
}

data class ServicePlanTemplateAllowedCodeRule(var allowed: MutableList<String>) : ServicePlanTemplateRule {
    override fun check(item: ServicePlanTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTemplateDockLengthRule(var maxLength: Int) : ServicePlanTemplateRule {
    override fun check(item: ServicePlanTemplate): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTemplateCapacityRangeRule(var min: Int, var max: Int) : ServicePlanTemplateRule {
    override fun check(item: ServicePlanTemplate): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTemplateRequiredCodeRule(var message: String) : ServicePlanTemplateRule {
    override fun check(item: ServicePlanTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTemplatePinnedGuardRule(var limit: Double) : ServicePlanTemplateRule {
    override fun check(item: ServicePlanTemplate): MutableList<String> {
        if (item.pinned && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTemplateRequiredNameRule(var message: String) : ServicePlanTemplateRule {
    override fun check(item: ServicePlanTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan template. */
data class ServicePlanTemplateValidator(var rules: MutableList<ServicePlanTemplateRule>) {
    fun validate(item: ServicePlanTemplate): MutableList<String> {
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

    fun requireValid(item: ServicePlanTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanTemplateValidator(): ServicePlanTemplateValidator {
    return ServicePlanTemplateValidator(
        mutableListOf<ServicePlanTemplateRule>(
            ServicePlanTemplateAllowedCodeRule(mutableListOf<String>("primary")),
            ServicePlanTemplateDockLengthRule(86400),
            ServicePlanTemplateCapacityRangeRule(64, 255),
            ServicePlanTemplateRequiredCodeRule("service plan template needs a value"),
            ServicePlanTemplatePinnedGuardRule(8.3),
            ServicePlanTemplateRequiredNameRule("service plan template needs this field"),
        ),
    )
}
