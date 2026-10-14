package com.acme.transport.freightpay.audit.cycle

/** A rule a audit threshold must pass. Check returns its problems, or none. */
interface AuditThresholdRule {
    fun check(item: AuditThreshold): MutableList<String>
}

data class AuditThresholdRequiredVehicleIdRule(var message: String) : AuditThresholdRule {
    override fun check(item: AuditThreshold): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditThresholdCodeLengthRule(var maxLength: Int) : AuditThresholdRule {
    override fun check(item: AuditThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditThresholdCodePrefixRule(var prefix: String) : AuditThresholdRule {
    override fun check(item: AuditThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditThresholdActiveGuardRule(var limit: Double) : AuditThresholdRule {
    override fun check(item: AuditThreshold): MutableList<String> {
        if (item.active && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit threshold. */
data class AuditThresholdValidator(var rules: MutableList<AuditThresholdRule>) {
    fun validate(item: AuditThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditThresholdValidator(): AuditThresholdValidator {
    return AuditThresholdValidator(
        mutableListOf<AuditThresholdRule>(
            AuditThresholdRequiredVehicleIdRule("audit threshold needs an entry"),
            AuditThresholdCodeLengthRule(32),
            AuditThresholdCodePrefixRule("X"),
            AuditThresholdActiveGuardRule(6.7),
        ),
    )
}
