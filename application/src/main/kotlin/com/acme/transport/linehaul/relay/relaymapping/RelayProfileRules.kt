package com.acme.transport.linehaul.relay.relaymapping

/** A rule a relay profile must pass. Check returns its problems, or none. */
interface RelayProfileRule {
    fun check(item: RelayProfile): MutableList<String>
}

data class RelayProfileRequiredCodeRule(var message: String) : RelayProfileRule {
    override fun check(item: RelayProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayProfileRequiredTitleRule(var message: String) : RelayProfileRule {
    override fun check(item: RelayProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayProfilePriorityRangeRule(var min: Int, var max: Int) : RelayProfileRule {
    override fun check(item: RelayProfile): MutableList<String> {
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

/** Runs every rule of a relay profile. */
data class RelayProfileValidator(var rules: MutableList<RelayProfileRule>) {
    fun validate(item: RelayProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayProfileValidator(): RelayProfileValidator {
    return RelayProfileValidator(
        mutableListOf<RelayProfileRule>(
            RelayProfileRequiredCodeRule("relay profile needs a value"),
            RelayProfileRequiredTitleRule("relay profile needs a value"),
            RelayProfilePriorityRangeRule(16, 120),
        ),
    )
}
