package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.text.normalize

/** A rule a audit cycle must pass. Check returns its problems, or none. */
interface AuditCycleRule {
    fun check(item: AuditCycle): MutableList<String>
}

data class AuditCycleNameLengthRule(var maxLength: Int) : AuditCycleRule {
    override fun check(item: AuditCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditCycleStartsAtRangeRule(var min: Int, var max: Int) : AuditCycleRule {
    override fun check(item: AuditCycle): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditCycleAllowedDepotRule(var allowed: MutableList<String>) : AuditCycleRule {
    override fun check(item: AuditCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit cycle. */
data class AuditCycleValidator(var rules: MutableList<AuditCycleRule>) {
    fun validate(item: AuditCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditCycleValidator(): AuditCycleValidator {
    return AuditCycleValidator(
        mutableListOf<AuditCycleRule>(
            AuditCycleNameLengthRule(86400),
            AuditCycleStartsAtRangeRule(120, 255),
            AuditCycleAllowedDepotRule(mutableListOf<String>()),
        ),
    )
}
