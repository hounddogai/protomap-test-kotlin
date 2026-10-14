package com.acme.transport.freightpay.accessorial.reservation

/** A rule a accessorial group must pass. Check returns its problems, or none. */
interface AccessorialGroupRule {
    fun check(item: AccessorialGroup): MutableList<String>
}

data class AccessorialGroupLimitRangeRule(var min: Int, var max: Int) : AccessorialGroupRule {
    override fun check(item: AccessorialGroup): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialGroupThresholdRangeRule(var min: Double, var max: Double) : AccessorialGroupRule {
    override fun check(item: AccessorialGroup): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialGroupPhoneLengthRule(var maxLength: Int) : AccessorialGroupRule {
    override fun check(item: AccessorialGroup): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial group. */
data class AccessorialGroupValidator(var rules: MutableList<AccessorialGroupRule>) {
    fun validate(item: AccessorialGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialGroupValidator(): AccessorialGroupValidator {
    return AccessorialGroupValidator(
        mutableListOf<AccessorialGroupRule>(
            AccessorialGroupLimitRangeRule(80, 120),
            AccessorialGroupThresholdRangeRule(5.2, 2180.3),
            AccessorialGroupPhoneLengthRule(80),
        ),
    )
}
