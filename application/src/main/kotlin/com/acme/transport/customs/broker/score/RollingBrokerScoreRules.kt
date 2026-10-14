package com.acme.transport.customs.broker.score

/** A rule a rolling broker score must pass. Check returns its problems, or none. */
interface RollingBrokerScoreRule {
    fun check(item: RollingBrokerScore): MutableList<String>
}

data class RollingBrokerScoreCodePrefixRule(var prefix: String) : RollingBrokerScoreRule {
    override fun check(item: RollingBrokerScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingBrokerScoreActiveGuardRule(var limit: Double) : RollingBrokerScoreRule {
    override fun check(item: RollingBrokerScore): MutableList<String> {
        if (item.active && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RollingBrokerScoreSequenceRangeRule(var min: Int, var max: Int) : RollingBrokerScoreRule {
    override fun check(item: RollingBrokerScore): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class RollingBrokerScoreRequiredCodeRule(var message: String) : RollingBrokerScoreRule {
    override fun check(item: RollingBrokerScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling broker score. */
data class RollingBrokerScoreValidator(var rules: MutableList<RollingBrokerScoreRule>) {
    fun validate(item: RollingBrokerScore): MutableList<String> {
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

    fun requireValid(item: RollingBrokerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingBrokerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingBrokerScoreValidator(): RollingBrokerScoreValidator {
    return RollingBrokerScoreValidator(
        mutableListOf<RollingBrokerScoreRule>(
            RollingBrokerScoreCodePrefixRule("REF"),
            RollingBrokerScoreActiveGuardRule(1.1),
            RollingBrokerScoreSequenceRangeRule(120, 1000),
            RollingBrokerScoreRequiredCodeRule("rolling broker score needs an entry"),
        ),
    )
}
