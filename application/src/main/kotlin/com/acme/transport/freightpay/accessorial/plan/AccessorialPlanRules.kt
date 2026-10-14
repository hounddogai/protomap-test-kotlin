package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.text.normalize

/** A rule a accessorial plan must pass. Check returns its problems, or none. */
interface AccessorialPlanRule {
    fun check(item: AccessorialPlan): MutableList<String>
}

data class AccessorialPlanQuantityRangeRule(var min: Int, var max: Int) : AccessorialPlanRule {
    override fun check(item: AccessorialPlan): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPlanOwnerIdLengthRule(var maxLength: Int) : AccessorialPlanRule {
    override fun check(item: AccessorialPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPlanLabelLengthRule(var maxLength: Int) : AccessorialPlanRule {
    override fun check(item: AccessorialPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPlanCodePrefixRule(var prefix: String) : AccessorialPlanRule {
    override fun check(item: AccessorialPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPlanCodeLengthRule(var maxLength: Int) : AccessorialPlanRule {
    override fun check(item: AccessorialPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialPlanAllowedCodeRule(var allowed: MutableList<String>) : AccessorialPlanRule {
    override fun check(item: AccessorialPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial plan. */
data class AccessorialPlanValidator(var rules: MutableList<AccessorialPlanRule>) {
    fun validate(item: AccessorialPlan): MutableList<String> {
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

    fun requireValid(item: AccessorialPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialPlanValidator(): AccessorialPlanValidator {
    return AccessorialPlanValidator(
        mutableListOf<AccessorialPlanRule>(
            AccessorialPlanQuantityRangeRule(64, 120),
            AccessorialPlanOwnerIdLengthRule(86400),
            AccessorialPlanLabelLengthRule(86400),
            AccessorialPlanCodePrefixRule("X"),
            AccessorialPlanCodeLengthRule(80),
            AccessorialPlanAllowedCodeRule(mutableListOf<String>("weighted", "weighted", "default")),
        ),
    )
}
