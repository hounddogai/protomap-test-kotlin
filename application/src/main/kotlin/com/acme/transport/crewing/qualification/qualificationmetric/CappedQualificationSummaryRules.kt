package com.acme.transport.crewing.qualification.qualificationmetric

/** A rule a capped qualification summary must pass. Check returns its problems, or none. */
interface CappedQualificationSummaryRule {
    fun check(item: CappedQualificationSummary): MutableList<String>
}

data class CappedQualificationSummaryRequiredCodeRule(var message: String) : CappedQualificationSummaryRule {
    override fun check(item: CappedQualificationSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationSummaryRouteCodeLengthRule(var maxLength: Int) : CappedQualificationSummaryRule {
    override fun check(item: CappedQualificationSummary): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationSummaryRequiredNameRule(var message: String) : CappedQualificationSummaryRule {
    override fun check(item: CappedQualificationSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationSummaryCodeLengthRule(var maxLength: Int) : CappedQualificationSummaryRule {
    override fun check(item: CappedQualificationSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationSummaryNameLengthRule(var maxLength: Int) : CappedQualificationSummaryRule {
    override fun check(item: CappedQualificationSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationSummaryUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : CappedQualificationSummaryRule {
    override fun check(item: CappedQualificationSummary): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped qualification summary. */
data class CappedQualificationSummaryValidator(var rules: MutableList<CappedQualificationSummaryRule>) {
    fun validate(item: CappedQualificationSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedQualificationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedQualificationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedQualificationSummaryValidator(): CappedQualificationSummaryValidator {
    return CappedQualificationSummaryValidator(
        mutableListOf<CappedQualificationSummaryRule>(
            CappedQualificationSummaryRequiredCodeRule("capped qualification summary needs a value"),
            CappedQualificationSummaryRouteCodeLengthRule(32),
            CappedQualificationSummaryRequiredNameRule("capped qualification summary needs this field"),
            CappedQualificationSummaryCodeLengthRule(1000),
            CappedQualificationSummaryNameLengthRule(80),
            CappedQualificationSummaryUpdatedAtRangeRule(32, 120),
        ),
    )
}
