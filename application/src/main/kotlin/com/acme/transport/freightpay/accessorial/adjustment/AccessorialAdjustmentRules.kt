package com.acme.transport.freightpay.accessorial.adjustment

/** A rule a accessorial adjustment must pass. Check returns its problems, or none. */
interface AccessorialAdjustmentRule {
    fun check(item: AccessorialAdjustment): MutableList<String>
}

data class AccessorialAdjustmentChecksumLengthRule(var maxLength: Int) : AccessorialAdjustmentRule {
    override fun check(item: AccessorialAdjustment): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialAdjustmentAttemptsRangeRule(var min: Int, var max: Int) : AccessorialAdjustmentRule {
    override fun check(item: AccessorialAdjustment): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialAdjustmentRatioRangeRule(var min: Double, var max: Double) : AccessorialAdjustmentRule {
    override fun check(item: AccessorialAdjustment): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial adjustment. */
data class AccessorialAdjustmentValidator(var rules: MutableList<AccessorialAdjustmentRule>) {
    fun validate(item: AccessorialAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialAdjustmentValidator(): AccessorialAdjustmentValidator {
    return AccessorialAdjustmentValidator(
        mutableListOf<AccessorialAdjustmentRule>(
            AccessorialAdjustmentChecksumLengthRule(3600),
            AccessorialAdjustmentAttemptsRangeRule(32, 86400),
            AccessorialAdjustmentRatioRangeRule(1.7, 1786.7),
        ),
    )
}
