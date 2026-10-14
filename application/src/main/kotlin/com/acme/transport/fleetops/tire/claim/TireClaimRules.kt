package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.text.normalize

/** A rule a tire claim must pass. Check returns its problems, or none. */
interface TireClaimRule {
    fun check(item: TireClaim): MutableList<String>
}

data class TireClaimAllowedCodeRule(var allowed: MutableList<String>) : TireClaimRule {
    override fun check(item: TireClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class TireClaimStopCountRangeRule(var min: Int, var max: Int) : TireClaimRule {
    override fun check(item: TireClaim): MutableList<String> {
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

data class TireClaimRequiredSourceRule(var message: String) : TireClaimRule {
    override fun check(item: TireClaim): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireClaimRequiredCodeRule(var message: String) : TireClaimRule {
    override fun check(item: TireClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireClaimOdometerRangeRule(var min: Int, var max: Int) : TireClaimRule {
    override fun check(item: TireClaim): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tire claim. */
data class TireClaimValidator(var rules: MutableList<TireClaimRule>) {
    fun validate(item: TireClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultTireClaimValidator(): TireClaimValidator {
    return TireClaimValidator(
        mutableListOf<TireClaimRule>(
            TireClaimAllowedCodeRule(mutableListOf<String>("scheduled", "default", "pending")),
            TireClaimStopCountRangeRule(1000, 86400),
            TireClaimRequiredSourceRule("tire claim needs an entry"),
            TireClaimRequiredCodeRule("tire claim needs a value"),
            TireClaimOdometerRangeRule(80, 1000),
        ),
    )
}
