package com.acme.transport.customs.broker.lenientbrokerestimate

/** A rule a broker claim must pass. Check returns its problems, or none. */
interface BrokerClaimRule {
    fun check(item: BrokerClaim): MutableList<String>
}

data class BrokerClaimRequiredCodeRule(var message: String) : BrokerClaimRule {
    override fun check(item: BrokerClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerClaimSourceLengthRule(var maxLength: Int) : BrokerClaimRule {
    override fun check(item: BrokerClaim): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerClaimNameLengthRule(var maxLength: Int) : BrokerClaimRule {
    override fun check(item: BrokerClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker claim. */
data class BrokerClaimValidator(var rules: MutableList<BrokerClaimRule>) {
    fun validate(item: BrokerClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerClaimValidator(): BrokerClaimValidator {
    return BrokerClaimValidator(
        mutableListOf<BrokerClaimRule>(
            BrokerClaimRequiredCodeRule("broker claim needs a value"),
            BrokerClaimSourceLengthRule(255),
            BrokerClaimNameLengthRule(16),
        ),
    )
}
