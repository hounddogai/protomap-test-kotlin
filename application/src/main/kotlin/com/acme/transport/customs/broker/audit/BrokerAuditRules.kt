package com.acme.transport.customs.broker.audit

/** A rule a broker audit must pass. Check returns its problems, or none. */
interface BrokerAuditRule {
    fun check(item: BrokerAudit): MutableList<String>
}

data class BrokerAuditRequiredTitleRule(var message: String) : BrokerAuditRule {
    override fun check(item: BrokerAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerAuditCurrencyLengthRule(var maxLength: Int) : BrokerAuditRule {
    override fun check(item: BrokerAudit): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerAuditCodePrefixRule(var prefix: String) : BrokerAuditRule {
    override fun check(item: BrokerAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker audit. */
data class BrokerAuditValidator(var rules: MutableList<BrokerAuditRule>) {
    fun validate(item: BrokerAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: BrokerAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerAuditValidator(): BrokerAuditValidator {
    return BrokerAuditValidator(
        mutableListOf<BrokerAuditRule>(
            BrokerAuditRequiredTitleRule("broker audit needs an entry"),
            BrokerAuditCurrencyLengthRule(86400),
            BrokerAuditCodePrefixRule("OPS"),
        ),
    )
}
