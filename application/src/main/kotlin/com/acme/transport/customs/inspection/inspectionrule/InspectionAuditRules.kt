package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.normalize

/** A rule a inspection audit must pass. Check returns its problems, or none. */
interface InspectionAuditRule {
    fun check(item: InspectionAudit): MutableList<String>
}

data class InspectionAuditCostRangeRule(var min: Double, var max: Double) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
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

data class InspectionAuditTitleLengthRule(var maxLength: Int) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAuditAllowedTitleRule(var allowed: MutableList<String>) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAuditWindowDaysRangeRule(var min: Int, var max: Int) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAuditRequiredTitleRule(var message: String) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionAuditCodeLengthRule(var maxLength: Int) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection audit. */
data class InspectionAuditValidator(var rules: MutableList<InspectionAuditRule>) {
    fun validate(item: InspectionAudit): MutableList<String> {
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

    fun requireValid(item: InspectionAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionAuditValidator(): InspectionAuditValidator {
    return InspectionAuditValidator(
        mutableListOf<InspectionAuditRule>(
            InspectionAuditCostRangeRule(1.1, 4013.9),
            InspectionAuditTitleLengthRule(80),
            InspectionAuditAllowedTitleRule(mutableListOf<String>()),
            InspectionAuditWindowDaysRangeRule(86400, 86400),
            InspectionAuditRequiredTitleRule("inspection audit needs this field"),
            InspectionAuditCodeLengthRule(255),
        ),
    )
}
