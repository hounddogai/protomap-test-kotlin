package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.normalize

/** A rule a defect budget must pass. Check returns its problems, or none. */
interface DefectBudgetRule {
    fun check(item: DefectBudget): MutableList<String>
}

data class DefectBudgetAllowedLabelRule(var allowed: MutableList<String>) : DefectBudgetRule {
    override fun check(item: DefectBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DefectBudgetCodeLengthRule(var maxLength: Int) : DefectBudgetRule {
    override fun check(item: DefectBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectBudgetRequiredLocaleRule(var message: String) : DefectBudgetRule {
    override fun check(item: DefectBudget): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect budget. */
data class DefectBudgetValidator(var rules: MutableList<DefectBudgetRule>) {
    fun validate(item: DefectBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectBudgetValidator(): DefectBudgetValidator {
    return DefectBudgetValidator(
        mutableListOf<DefectBudgetRule>(
            DefectBudgetAllowedLabelRule(mutableListOf<String>()),
            DefectBudgetCodeLengthRule(3600),
            DefectBudgetRequiredLocaleRule("defect budget needs this field"),
        ),
    )
}
