package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.normalize

/** A rule a tiered near miss profile must pass. Check returns its problems, or none. */
interface TieredNearMissProfileRule {
    fun check(item: TieredNearMissProfile): MutableList<String>
}

data class TieredNearMissProfileAllowedCodeRule(var allowed: MutableList<String>) : TieredNearMissProfileRule {
    override fun check(item: TieredNearMissProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class TieredNearMissProfileStartsAtRangeRule(var min: Int, var max: Int) : TieredNearMissProfileRule {
    override fun check(item: TieredNearMissProfile): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredNearMissProfileCodeLengthRule(var maxLength: Int) : TieredNearMissProfileRule {
    override fun check(item: TieredNearMissProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredNearMissProfileUpdatedAtRangeRule(var min: Int, var max: Int) : TieredNearMissProfileRule {
    override fun check(item: TieredNearMissProfile): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredNearMissProfileWeightRangeRule(var min: Double, var max: Double) : TieredNearMissProfileRule {
    override fun check(item: TieredNearMissProfile): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered near miss profile. */
data class TieredNearMissProfileValidator(var rules: MutableList<TieredNearMissProfileRule>) {
    fun validate(item: TieredNearMissProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredNearMissProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredNearMissProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredNearMissProfileValidator(): TieredNearMissProfileValidator {
    return TieredNearMissProfileValidator(
        mutableListOf<TieredNearMissProfileRule>(
            TieredNearMissProfileAllowedCodeRule(mutableListOf<String>("pending", "default")),
            TieredNearMissProfileStartsAtRangeRule(1000, 86400),
            TieredNearMissProfileCodeLengthRule(1000),
            TieredNearMissProfileUpdatedAtRangeRule(120, 3600),
            TieredNearMissProfileWeightRangeRule(3.2, 1629.2),
        ),
    )
}
