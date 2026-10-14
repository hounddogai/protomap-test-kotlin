package com.acme.transport.customs.broker.checkpoint

/** A rule a capped broker checkpoint must pass. Check returns its problems, or none. */
interface CappedBrokerCheckpointRule {
    fun check(item: CappedBrokerCheckpoint): MutableList<String>
}

data class CappedBrokerCheckpointRequiredNameRule(var message: String) : CappedBrokerCheckpointRule {
    override fun check(item: CappedBrokerCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerCheckpointCreatedAtRangeRule(var min: Int, var max: Int) : CappedBrokerCheckpointRule {
    override fun check(item: CappedBrokerCheckpoint): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerCheckpointRequiredSummaryRule(var message: String) : CappedBrokerCheckpointRule {
    override fun check(item: CappedBrokerCheckpoint): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped broker checkpoint. */
data class CappedBrokerCheckpointValidator(var rules: MutableList<CappedBrokerCheckpointRule>) {
    fun validate(item: CappedBrokerCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedBrokerCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBrokerCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBrokerCheckpointValidator(): CappedBrokerCheckpointValidator {
    return CappedBrokerCheckpointValidator(
        mutableListOf<CappedBrokerCheckpointRule>(
            CappedBrokerCheckpointRequiredNameRule("capped broker checkpoint needs a value"),
            CappedBrokerCheckpointCreatedAtRangeRule(120, 3600),
            CappedBrokerCheckpointRequiredSummaryRule("capped broker checkpoint needs an entry"),
        ),
    )
}
