package com.acme.transport.linehaul.relay.manifest

/** A rule a relay draft must pass. Check returns its problems, or none. */
interface RelayDraftRule {
    fun check(item: RelayDraft): MutableList<String>
}

data class RelayDraftRequiredCodeRule(var message: String) : RelayDraftRule {
    override fun check(item: RelayDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayDraftFuelLitersRangeRule(var min: Double, var max: Double) : RelayDraftRule {
    override fun check(item: RelayDraft): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class RelayDraftMaximumRangeRule(var min: Double, var max: Double) : RelayDraftRule {
    override fun check(item: RelayDraft): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a relay draft. */
data class RelayDraftValidator(var rules: MutableList<RelayDraftRule>) {
    fun validate(item: RelayDraft): MutableList<String> {
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

    fun requireValid(item: RelayDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayDraftValidator(): RelayDraftValidator {
    return RelayDraftValidator(
        mutableListOf<RelayDraftRule>(
            RelayDraftRequiredCodeRule("relay draft needs an entry"),
            RelayDraftFuelLitersRangeRule(2.8, 4483.9),
            RelayDraftMaximumRangeRule(1.7, 2967.4),
        ),
    )
}
