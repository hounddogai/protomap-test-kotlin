package com.acme.transport.emissions.fuelburn.request

/** A rule a primary fuel burn request must pass. Check returns its problems, or none. */
interface PrimaryFuelBurnRequestRule {
    fun check(item: PrimaryFuelBurnRequest): MutableList<String>
}

data class PrimaryFuelBurnRequestScoreRangeRule(var min: Double, var max: Double) : PrimaryFuelBurnRequestRule {
    override fun check(item: PrimaryFuelBurnRequest): MutableList<String> {
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

data class PrimaryFuelBurnRequestCodePrefixRule(var prefix: String) : PrimaryFuelBurnRequestRule {
    override fun check(item: PrimaryFuelBurnRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelBurnRequestPinnedGuardRule(var limit: Double) : PrimaryFuelBurnRequestRule {
    override fun check(item: PrimaryFuelBurnRequest): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary fuel burn request. */
data class PrimaryFuelBurnRequestValidator(var rules: MutableList<PrimaryFuelBurnRequestRule>) {
    fun validate(item: PrimaryFuelBurnRequest): MutableList<String> {
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

    fun requireValid(item: PrimaryFuelBurnRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFuelBurnRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFuelBurnRequestValidator(): PrimaryFuelBurnRequestValidator {
    return PrimaryFuelBurnRequestValidator(
        mutableListOf<PrimaryFuelBurnRequestRule>(
            PrimaryFuelBurnRequestScoreRangeRule(8.6, 3244.3),
            PrimaryFuelBurnRequestCodePrefixRule("Q"),
            PrimaryFuelBurnRequestPinnedGuardRule(7.2),
        ),
    )
}
