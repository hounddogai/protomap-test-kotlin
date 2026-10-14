package com.acme.transport.safety.violation.baseline

/** A rule a capped violation baseline must pass. Check returns its problems, or none. */
interface CappedViolationBaselineRule {
    fun check(item: CappedViolationBaseline): MutableList<String>
}

data class CappedViolationBaselineGraceDaysRangeRule(var min: Int, var max: Int) : CappedViolationBaselineRule {
    override fun check(item: CappedViolationBaseline): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationBaselineRequiredNameRule(var message: String) : CappedViolationBaselineRule {
    override fun check(item: CappedViolationBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationBaselineRequiredPhoneRule(var message: String) : CappedViolationBaselineRule {
    override fun check(item: CappedViolationBaseline): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped violation baseline. */
data class CappedViolationBaselineValidator(var rules: MutableList<CappedViolationBaselineRule>) {
    fun validate(item: CappedViolationBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedViolationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedViolationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedViolationBaselineValidator(): CappedViolationBaselineValidator {
    return CappedViolationBaselineValidator(
        mutableListOf<CappedViolationBaselineRule>(
            CappedViolationBaselineGraceDaysRangeRule(16, 1000),
            CappedViolationBaselineRequiredNameRule("capped violation baseline needs an entry"),
            CappedViolationBaselineRequiredPhoneRule("capped violation baseline needs this field"),
        ),
    )
}
