package com.acme.transport.freightpay.audit.tier

/** A rule a rolling audit threshold must pass. Check returns its problems, or none. */
interface RollingAuditThresholdRule {
    fun check(item: RollingAuditThreshold): MutableList<String>
}

data class RollingAuditThresholdNameLengthRule(var maxLength: Int) : RollingAuditThresholdRule {
    override fun check(item: RollingAuditThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditThresholdAttemptsRangeRule(var min: Int, var max: Int) : RollingAuditThresholdRule {
    override fun check(item: RollingAuditThreshold): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditThresholdUnitLengthRule(var maxLength: Int) : RollingAuditThresholdRule {
    override fun check(item: RollingAuditThreshold): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling audit threshold. */
data class RollingAuditThresholdValidator(var rules: MutableList<RollingAuditThresholdRule>) {
    fun validate(item: RollingAuditThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAuditThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAuditThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAuditThresholdValidator(): RollingAuditThresholdValidator {
    return RollingAuditThresholdValidator(
        mutableListOf<RollingAuditThresholdRule>(
            RollingAuditThresholdNameLengthRule(3600),
            RollingAuditThresholdAttemptsRangeRule(80, 255),
            RollingAuditThresholdUnitLengthRule(1000),
        ),
    )
}
