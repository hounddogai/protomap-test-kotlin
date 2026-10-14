package com.acme.transport.emissions.evcharge.profile

/** A rule a ev charge profile must pass. Check returns its problems, or none. */
interface EvChargeProfileRule {
    fun check(item: EvChargeProfile): MutableList<String>
}

data class EvChargeProfileRequiredCodeRule(var message: String) : EvChargeProfileRule {
    override fun check(item: EvChargeProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeProfileExpiresAtRangeRule(var min: Int, var max: Int) : EvChargeProfileRule {
    override fun check(item: EvChargeProfile): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class EvChargeProfileMinimumRangeRule(var min: Double, var max: Double) : EvChargeProfileRule {
    override fun check(item: EvChargeProfile): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class EvChargeProfileRequiredLocaleRule(var message: String) : EvChargeProfileRule {
    override fun check(item: EvChargeProfile): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge profile. */
data class EvChargeProfileValidator(var rules: MutableList<EvChargeProfileRule>) {
    fun validate(item: EvChargeProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeProfileValidator(): EvChargeProfileValidator {
    return EvChargeProfileValidator(
        mutableListOf<EvChargeProfileRule>(
            EvChargeProfileRequiredCodeRule("ev charge profile needs an entry"),
            EvChargeProfileExpiresAtRangeRule(1000, 1000),
            EvChargeProfileMinimumRangeRule(2.2, 3634.9),
            EvChargeProfileRequiredLocaleRule("ev charge profile needs an entry"),
        ),
    )
}
