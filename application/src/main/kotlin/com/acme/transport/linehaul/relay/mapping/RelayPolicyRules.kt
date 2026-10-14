package com.acme.transport.linehaul.relay.mapping

/** A rule a relay policy must pass. Check returns its problems, or none. */
interface RelayPolicyRule {
    fun check(item: RelayPolicy): MutableList<String>
}

data class RelayPolicyRequiredLabelRule(var message: String) : RelayPolicyRule {
    override fun check(item: RelayPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayPolicyRequiredDriverIdRule(var message: String) : RelayPolicyRule {
    override fun check(item: RelayPolicy): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayPolicyQuantityRangeRule(var min: Int, var max: Int) : RelayPolicyRule {
    override fun check(item: RelayPolicy): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay policy. */
data class RelayPolicyValidator(var rules: MutableList<RelayPolicyRule>) {
    fun validate(item: RelayPolicy): MutableList<String> {
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

    fun requireValid(item: RelayPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayPolicyValidator(): RelayPolicyValidator {
    return RelayPolicyValidator(
        mutableListOf<RelayPolicyRule>(
            RelayPolicyRequiredLabelRule("relay policy needs an entry"),
            RelayPolicyRequiredDriverIdRule("relay policy needs an entry"),
            RelayPolicyQuantityRangeRule(120, 120),
        ),
    )
}
