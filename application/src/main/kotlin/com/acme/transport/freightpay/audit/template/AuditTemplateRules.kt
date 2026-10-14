package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.text.normalize

/** A rule a audit template must pass. Check returns its problems, or none. */
interface AuditTemplateRule {
    fun check(item: AuditTemplate): MutableList<String>
}

data class AuditTemplateRequiredShiftCodeRule(var message: String) : AuditTemplateRule {
    override fun check(item: AuditTemplate): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditTemplateAllowedLabelRule(var allowed: MutableList<String>) : AuditTemplateRule {
    override fun check(item: AuditTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class AuditTemplateRequiredLabelRule(var message: String) : AuditTemplateRule {
    override fun check(item: AuditTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditTemplatePriorityRangeRule(var min: Int, var max: Int) : AuditTemplateRule {
    override fun check(item: AuditTemplate): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit template. */
data class AuditTemplateValidator(var rules: MutableList<AuditTemplateRule>) {
    fun validate(item: AuditTemplate): MutableList<String> {
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

    fun requireValid(item: AuditTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditTemplateValidator(): AuditTemplateValidator {
    return AuditTemplateValidator(
        mutableListOf<AuditTemplateRule>(
            AuditTemplateRequiredShiftCodeRule("audit template needs an entry"),
            AuditTemplateAllowedLabelRule(mutableListOf<String>()),
            AuditTemplateRequiredLabelRule("audit template needs an entry"),
            AuditTemplatePriorityRangeRule(120, 1000),
        ),
    )
}
