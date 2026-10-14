package com.acme.transport.emissions.carbonreport.reportscore

/** A rule a capped carbon report assignment must pass. Check returns its problems, or none. */
interface CappedCarbonReportAssignmentRule {
    fun check(item: CappedCarbonReportAssignment): MutableList<String>
}

data class CappedCarbonReportAssignmentCodePrefixRule(var prefix: String) : CappedCarbonReportAssignmentRule {
    override fun check(item: CappedCarbonReportAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportAssignmentDescriptionLengthRule(
    var maxLength: Int,
) : CappedCarbonReportAssignmentRule {
    override fun check(item: CappedCarbonReportAssignment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCarbonReportAssignmentRequiredCodeRule(
    var message: String,
) : CappedCarbonReportAssignmentRule {
    override fun check(item: CappedCarbonReportAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped carbon report assignment. */
data class CappedCarbonReportAssignmentValidator(var rules: MutableList<CappedCarbonReportAssignmentRule>) {
    fun validate(item: CappedCarbonReportAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCarbonReportAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCarbonReportAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCarbonReportAssignmentValidator(): CappedCarbonReportAssignmentValidator {
    return CappedCarbonReportAssignmentValidator(
        mutableListOf<CappedCarbonReportAssignmentRule>(
            CappedCarbonReportAssignmentCodePrefixRule("ACM"),
            CappedCarbonReportAssignmentDescriptionLengthRule(64),
            CappedCarbonReportAssignmentRequiredCodeRule("capped carbon report assignment needs an entry"),
        ),
    )
}
