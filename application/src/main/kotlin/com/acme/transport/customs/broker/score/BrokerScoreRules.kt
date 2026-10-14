package com.acme.transport.customs.broker.score

/** A rule a broker score must pass. Check returns its problems, or none. */
interface BrokerScoreRule {
    fun check(item: BrokerScore): MutableList<String>
}

data class BrokerScoreRequiredCodeRule(var message: String) : BrokerScoreRule {
    override fun check(item: BrokerScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerScoreRequiredUnitRule(var message: String) : BrokerScoreRule {
    override fun check(item: BrokerScore): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerScoreCodeLengthRule(var maxLength: Int) : BrokerScoreRule {
    override fun check(item: BrokerScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker score. */
data class BrokerScoreValidator(var rules: MutableList<BrokerScoreRule>) {
    fun validate(item: BrokerScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerScoreValidator(): BrokerScoreValidator {
    return BrokerScoreValidator(
        mutableListOf<BrokerScoreRule>(
            BrokerScoreRequiredCodeRule("broker score needs an entry"),
            BrokerScoreRequiredUnitRule("broker score needs an entry"),
            BrokerScoreCodeLengthRule(16),
        ),
    )
}
