package com.acme.transport.customs.broker.quota

/** A rule a default broker quota must pass. Check returns its problems, or none. */
interface DefaultBrokerQuotaRule {
    fun check(item: DefaultBrokerQuota): MutableList<String>
}

data class DefaultBrokerQuotaNameLengthRule(var maxLength: Int) : DefaultBrokerQuotaRule {
    override fun check(item: DefaultBrokerQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultBrokerQuotaRequiredSummaryRule(var message: String) : DefaultBrokerQuotaRule {
    override fun check(item: DefaultBrokerQuota): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultBrokerQuotaSummaryLengthRule(var maxLength: Int) : DefaultBrokerQuotaRule {
    override fun check(item: DefaultBrokerQuota): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default broker quota. */
data class DefaultBrokerQuotaValidator(var rules: MutableList<DefaultBrokerQuotaRule>) {
    fun validate(item: DefaultBrokerQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultBrokerQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultBrokerQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultBrokerQuotaValidator(): DefaultBrokerQuotaValidator {
    return DefaultBrokerQuotaValidator(
        mutableListOf<DefaultBrokerQuotaRule>(
            DefaultBrokerQuotaNameLengthRule(64),
            DefaultBrokerQuotaRequiredSummaryRule("default broker quota needs a value"),
            DefaultBrokerQuotaSummaryLengthRule(255),
        ),
    )
}
