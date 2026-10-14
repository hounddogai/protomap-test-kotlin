package com.acme.transport.customs.inspection.schedule

/** A rule a inspection variance must pass. Check returns its problems, or none. */
interface InspectionVarianceRule {
    fun check(item: InspectionVariance): MutableList<String>
}

data class InspectionVarianceNameLengthRule(var maxLength: Int) : InspectionVarianceRule {
    override fun check(item: InspectionVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionVarianceRequiredNameRule(var message: String) : InspectionVarianceRule {
    override fun check(item: InspectionVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionVarianceCodePrefixRule(var prefix: String) : InspectionVarianceRule {
    override fun check(item: InspectionVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection variance. */
data class InspectionVarianceValidator(var rules: MutableList<InspectionVarianceRule>) {
    fun validate(item: InspectionVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionVarianceValidator(): InspectionVarianceValidator {
    return InspectionVarianceValidator(
        mutableListOf<InspectionVarianceRule>(
            InspectionVarianceNameLengthRule(80),
            InspectionVarianceRequiredNameRule("inspection variance needs a value"),
            InspectionVarianceCodePrefixRule("Q"),
        ),
    )
}
