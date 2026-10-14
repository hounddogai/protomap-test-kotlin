package com.acme.transport.customs.broker.snapshot

/** A rule a rolling broker quota must pass. Check returns its problems, or none. */
interface RollingBrokerQuotaRule {
    fun check(item: RollingBrokerQuota): MutableList<String>
}

data class RollingBrokerQuotaCodeLengthRule(var maxLength: Int) : RollingBrokerQuotaRule {
    override fun check(item: RollingBrokerQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingBrokerQuotaPriorityRangeRule(var min: Int, var max: Int) : RollingBrokerQuotaRule {
    override fun check(item: RollingBrokerQuota): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class RollingBrokerQuotaTitleLengthRule(var maxLength: Int) : RollingBrokerQuotaRule {
    override fun check(item: RollingBrokerQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling broker quota. */
data class RollingBrokerQuotaValidator(var rules: MutableList<RollingBrokerQuotaRule>) {
    fun validate(item: RollingBrokerQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingBrokerQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingBrokerQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingBrokerQuotaValidator(): RollingBrokerQuotaValidator {
    return RollingBrokerQuotaValidator(
        mutableListOf<RollingBrokerQuotaRule>(
            RollingBrokerQuotaCodeLengthRule(255),
            RollingBrokerQuotaPriorityRangeRule(80, 80),
            RollingBrokerQuotaTitleLengthRule(255),
        ),
    )
}
