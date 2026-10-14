package com.acme.transport.linehaul.relay.quota

/** A rule a lenient relay claim must pass. Check returns its problems, or none. */
interface LenientRelayClaimRule {
    fun check(item: LenientRelayClaim): MutableList<String>
}

data class LenientRelayClaimRequiredLabelRule(var message: String) : LenientRelayClaimRule {
    override fun check(item: LenientRelayClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayClaimStopCountRangeRule(var min: Int, var max: Int) : LenientRelayClaimRule {
    override fun check(item: LenientRelayClaim): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class LenientRelayClaimCapacityRangeRule(var min: Int, var max: Int) : LenientRelayClaimRule {
    override fun check(item: LenientRelayClaim): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LenientRelayClaimSourceLengthRule(var maxLength: Int) : LenientRelayClaimRule {
    override fun check(item: LenientRelayClaim): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient relay claim. */
data class LenientRelayClaimValidator(var rules: MutableList<LenientRelayClaimRule>) {
    fun validate(item: LenientRelayClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRelayClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRelayClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRelayClaimValidator(): LenientRelayClaimValidator {
    return LenientRelayClaimValidator(
        mutableListOf<LenientRelayClaimRule>(
            LenientRelayClaimRequiredLabelRule("lenient relay claim needs a value"),
            LenientRelayClaimStopCountRangeRule(32, 80),
            LenientRelayClaimCapacityRangeRule(255, 1000),
            LenientRelayClaimSourceLengthRule(3600),
        ),
    )
}
