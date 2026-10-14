package com.acme.transport.freightpay.accessorial.cycle

/** A rule a accessorial variance must pass. Check returns its problems, or none. */
interface AccessorialVarianceRule {
    fun check(item: AccessorialVariance): MutableList<String>
}

data class AccessorialVarianceLabelLengthRule(var maxLength: Int) : AccessorialVarianceRule {
    override fun check(item: AccessorialVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialVarianceRequiredLabelRule(var message: String) : AccessorialVarianceRule {
    override fun check(item: AccessorialVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialVarianceLimitRangeRule(var min: Int, var max: Int) : AccessorialVarianceRule {
    override fun check(item: AccessorialVariance): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial variance. */
data class AccessorialVarianceValidator(var rules: MutableList<AccessorialVarianceRule>) {
    fun validate(item: AccessorialVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialVarianceValidator(): AccessorialVarianceValidator {
    return AccessorialVarianceValidator(
        mutableListOf<AccessorialVarianceRule>(
            AccessorialVarianceLabelLengthRule(80),
            AccessorialVarianceRequiredLabelRule("accessorial variance needs an entry"),
            AccessorialVarianceLimitRangeRule(16, 86400),
        ),
    )
}
