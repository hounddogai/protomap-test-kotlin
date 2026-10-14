package com.acme.transport.emissions.evcharge.baseline

/** A rule a ev charge request must pass. Check returns its problems, or none. */
interface EvChargeRequestRule {
    fun check(item: EvChargeRequest): MutableList<String>
}

data class EvChargeRequestExternalRefLengthRule(var maxLength: Int) : EvChargeRequestRule {
    override fun check(item: EvChargeRequest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeRequestRequiredExternalRefRule(var message: String) : EvChargeRequestRule {
    override fun check(item: EvChargeRequest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeRequestCapacityRangeRule(var min: Int, var max: Int) : EvChargeRequestRule {
    override fun check(item: EvChargeRequest): MutableList<String> {
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

data class EvChargeRequestScoreRangeRule(var min: Double, var max: Double) : EvChargeRequestRule {
    override fun check(item: EvChargeRequest): MutableList<String> {
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

data class EvChargeRequestAmountRangeRule(var min: Double, var max: Double) : EvChargeRequestRule {
    override fun check(item: EvChargeRequest): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class EvChargeRequestRequiredCodeRule(var message: String) : EvChargeRequestRule {
    override fun check(item: EvChargeRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge request. */
data class EvChargeRequestValidator(var rules: MutableList<EvChargeRequestRule>) {
    fun validate(item: EvChargeRequest): MutableList<String> {
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

    fun requireValid(item: EvChargeRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeRequestValidator(): EvChargeRequestValidator {
    return EvChargeRequestValidator(
        mutableListOf<EvChargeRequestRule>(
            EvChargeRequestExternalRefLengthRule(3600),
            EvChargeRequestRequiredExternalRefRule("ev charge request needs this field"),
            EvChargeRequestCapacityRangeRule(3600, 3600),
            EvChargeRequestScoreRangeRule(2.4, 4041.1),
            EvChargeRequestAmountRangeRule(9.2, 3258.8),
            EvChargeRequestRequiredCodeRule("ev charge request needs an entry"),
        ),
    )
}
