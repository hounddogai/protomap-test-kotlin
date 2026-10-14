package com.acme.transport.crewing.qualification.digest

/** A rule a qualification summary must pass. Check returns its problems, or none. */
interface QualificationSummaryRule {
    fun check(item: QualificationSummary): MutableList<String>
}

data class QualificationSummaryBudgetRangeRule(var min: Double, var max: Double) : QualificationSummaryRule {
    override fun check(item: QualificationSummary): MutableList<String> {
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

data class QualificationSummaryRequiredCodeRule(var message: String) : QualificationSummaryRule {
    override fun check(item: QualificationSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationSummaryRequiredOwnerIdRule(var message: String) : QualificationSummaryRule {
    override fun check(item: QualificationSummary): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification summary. */
data class QualificationSummaryValidator(var rules: MutableList<QualificationSummaryRule>) {
    fun validate(item: QualificationSummary): MutableList<String> {
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

    fun requireValid(item: QualificationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationSummaryValidator(): QualificationSummaryValidator {
    return QualificationSummaryValidator(
        mutableListOf<QualificationSummaryRule>(
            QualificationSummaryBudgetRangeRule(7.4, 3505.4),
            QualificationSummaryRequiredCodeRule("qualification summary needs an entry"),
            QualificationSummaryRequiredOwnerIdRule("qualification summary needs a value"),
        ),
    )
}
