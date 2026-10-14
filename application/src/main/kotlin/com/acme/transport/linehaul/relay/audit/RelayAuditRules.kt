package com.acme.transport.linehaul.relay.audit

/** A rule a relay audit must pass. Check returns its problems, or none. */
interface RelayAuditRule {
    fun check(item: RelayAudit): MutableList<String>
}

data class RelayAuditNameLengthRule(var maxLength: Int) : RelayAuditRule {
    override fun check(item: RelayAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayAuditRequiredCodeRule(var message: String) : RelayAuditRule {
    override fun check(item: RelayAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayAuditEndsAtRangeRule(var min: Int, var max: Int) : RelayAuditRule {
    override fun check(item: RelayAudit): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayAuditCodePrefixRule(var prefix: String) : RelayAuditRule {
    override fun check(item: RelayAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelayAuditRequiredNameRule(var message: String) : RelayAuditRule {
    override fun check(item: RelayAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay audit. */
data class RelayAuditValidator(var rules: MutableList<RelayAuditRule>) {
    fun validate(item: RelayAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayAuditValidator(): RelayAuditValidator {
    return RelayAuditValidator(
        mutableListOf<RelayAuditRule>(
            RelayAuditNameLengthRule(86400),
            RelayAuditRequiredCodeRule("relay audit needs this field"),
            RelayAuditEndsAtRangeRule(32, 64),
            RelayAuditCodePrefixRule("REF"),
            RelayAuditRequiredNameRule("relay audit needs an entry"),
        ),
    )
}
