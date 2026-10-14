package com.acme.transport.crewing.qualification.allocation

/** A rule a default qualification summary must pass. Check returns its problems, or none. */
interface DefaultQualificationSummaryRule {
    fun check(item: DefaultQualificationSummary): MutableList<String>
}

data class DefaultQualificationSummaryRouteCodeLengthRule(
    var maxLength: Int,
) : DefaultQualificationSummaryRule {
    override fun check(item: DefaultQualificationSummary): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultQualificationSummaryCodeLengthRule(var maxLength: Int) : DefaultQualificationSummaryRule {
    override fun check(item: DefaultQualificationSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultQualificationSummaryRequiredCodeRule(var message: String) : DefaultQualificationSummaryRule {
    override fun check(item: DefaultQualificationSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default qualification summary. */
data class DefaultQualificationSummaryValidator(var rules: MutableList<DefaultQualificationSummaryRule>) {
    fun validate(item: DefaultQualificationSummary): MutableList<String> {
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

    fun requireValid(item: DefaultQualificationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultQualificationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultQualificationSummaryValidator(): DefaultQualificationSummaryValidator {
    return DefaultQualificationSummaryValidator(
        mutableListOf<DefaultQualificationSummaryRule>(
            DefaultQualificationSummaryRouteCodeLengthRule(1000),
            DefaultQualificationSummaryCodeLengthRule(255),
            DefaultQualificationSummaryRequiredCodeRule("default qualification summary needs a value"),
        ),
    )
}
