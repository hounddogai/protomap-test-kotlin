package com.acme.transport.fleetops.defect.defectsummary

/** A rule a default defect summary must pass. Check returns its problems, or none. */
interface DefaultDefectSummaryRule {
    fun check(item: DefaultDefectSummary): MutableList<String>
}

data class DefaultDefectSummaryRequiredTitleRule(var message: String) : DefaultDefectSummaryRule {
    override fun check(item: DefaultDefectSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectSummaryCodeLengthRule(var maxLength: Int) : DefaultDefectSummaryRule {
    override fun check(item: DefaultDefectSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectSummaryRequiredPhoneRule(var message: String) : DefaultDefectSummaryRule {
    override fun check(item: DefaultDefectSummary): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectSummaryPhoneLengthRule(var maxLength: Int) : DefaultDefectSummaryRule {
    override fun check(item: DefaultDefectSummary): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectSummaryRequiredLabelRule(var message: String) : DefaultDefectSummaryRule {
    override fun check(item: DefaultDefectSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default defect summary. */
data class DefaultDefectSummaryValidator(var rules: MutableList<DefaultDefectSummaryRule>) {
    fun validate(item: DefaultDefectSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDefectSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDefectSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDefectSummaryValidator(): DefaultDefectSummaryValidator {
    return DefaultDefectSummaryValidator(
        mutableListOf<DefaultDefectSummaryRule>(
            DefaultDefectSummaryRequiredTitleRule("default defect summary needs a value"),
            DefaultDefectSummaryCodeLengthRule(64),
            DefaultDefectSummaryRequiredPhoneRule("default defect summary needs this field"),
            DefaultDefectSummaryPhoneLengthRule(255),
            DefaultDefectSummaryRequiredLabelRule("default defect summary needs an entry"),
        ),
    )
}
