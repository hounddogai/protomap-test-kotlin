package com.acme.transport.emissions.carbonreport.reportmapping

/** A rule a primary carbon report policy must pass. Check returns its problems, or none. */
interface PrimaryCarbonReportPolicyRule {
    fun check(item: PrimaryCarbonReportPolicy): MutableList<String>
}

data class PrimaryCarbonReportPolicyCodeLengthRule(var maxLength: Int) : PrimaryCarbonReportPolicyRule {
    override fun check(item: PrimaryCarbonReportPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportPolicyRequiredCodeRule(var message: String) : PrimaryCarbonReportPolicyRule {
    override fun check(item: PrimaryCarbonReportPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportPolicyRetriesRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryCarbonReportPolicyRule {
    override fun check(item: PrimaryCarbonReportPolicy): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary carbon report policy. */
data class PrimaryCarbonReportPolicyValidator(var rules: MutableList<PrimaryCarbonReportPolicyRule>) {
    fun validate(item: PrimaryCarbonReportPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryCarbonReportPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCarbonReportPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCarbonReportPolicyValidator(): PrimaryCarbonReportPolicyValidator {
    return PrimaryCarbonReportPolicyValidator(
        mutableListOf<PrimaryCarbonReportPolicyRule>(
            PrimaryCarbonReportPolicyCodeLengthRule(3600),
            PrimaryCarbonReportPolicyRequiredCodeRule("primary carbon report policy needs an entry"),
            PrimaryCarbonReportPolicyRetriesRangeRule(120, 3600),
        ),
    )
}
