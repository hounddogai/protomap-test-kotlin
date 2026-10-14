package com.acme.transport.safety.violation.proposal

/** A rule a regional violation digest must pass. Check returns its problems, or none. */
interface RegionalViolationDigestRule {
    fun check(item: RegionalViolationDigest): MutableList<String>
}

data class RegionalViolationDigestRequiredSegmentRule(var message: String) : RegionalViolationDigestRule {
    override fun check(item: RegionalViolationDigest): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationDigestBudgetRangeRule(
    var min: Double,
    var max: Double,
) : RegionalViolationDigestRule {
    override fun check(item: RegionalViolationDigest): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationDigestRequiredCodeRule(var message: String) : RegionalViolationDigestRule {
    override fun check(item: RegionalViolationDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional violation digest. */
data class RegionalViolationDigestValidator(var rules: MutableList<RegionalViolationDigestRule>) {
    fun validate(item: RegionalViolationDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalViolationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalViolationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalViolationDigestValidator(): RegionalViolationDigestValidator {
    return RegionalViolationDigestValidator(
        mutableListOf<RegionalViolationDigestRule>(
            RegionalViolationDigestRequiredSegmentRule("regional violation digest needs a value"),
            RegionalViolationDigestBudgetRangeRule(3.7, 841.4),
            RegionalViolationDigestRequiredCodeRule("regional violation digest needs this field"),
        ),
    )
}
