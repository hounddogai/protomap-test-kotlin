package com.acme.transport.customs.broker.review

import com.acme.transport.kit.text.normalize

/** A rule a broker cycle must pass. Check returns its problems, or none. */
interface BrokerCycleRule {
    fun check(item: BrokerCycle): MutableList<String>
}

data class BrokerCycleScoreRangeRule(var min: Double, var max: Double) : BrokerCycleRule {
    override fun check(item: BrokerCycle): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class BrokerCycleAllowedNameRule(var allowed: MutableList<String>) : BrokerCycleRule {
    override fun check(item: BrokerCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class BrokerCycleRequiredNameRule(var message: String) : BrokerCycleRule {
    override fun check(item: BrokerCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker cycle. */
data class BrokerCycleValidator(var rules: MutableList<BrokerCycleRule>) {
    fun validate(item: BrokerCycle): MutableList<String> {
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

    fun requireValid(item: BrokerCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerCycleValidator(): BrokerCycleValidator {
    return BrokerCycleValidator(
        mutableListOf<BrokerCycleRule>(
            BrokerCycleScoreRangeRule(6.3, 3680.9),
            BrokerCycleAllowedNameRule(mutableListOf<String>("legacy")),
            BrokerCycleRequiredNameRule("broker cycle needs an entry"),
        ),
    )
}
