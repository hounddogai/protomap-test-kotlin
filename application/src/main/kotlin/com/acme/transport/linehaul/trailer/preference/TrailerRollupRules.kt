package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.text.normalize

/** A rule a trailer rollup must pass. Check returns its problems, or none. */
interface TrailerRollupRule {
    fun check(item: TrailerRollup): MutableList<String>
}

data class TrailerRollupLaneLengthRule(var maxLength: Int) : TrailerRollupRule {
    override fun check(item: TrailerRollup): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRollupAllowedCarrierRule(var allowed: MutableList<String>) : TrailerRollupRule {
    override fun check(item: TrailerRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRollupCodeLengthRule(var maxLength: Int) : TrailerRollupRule {
    override fun check(item: TrailerRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRollupNameLengthRule(var maxLength: Int) : TrailerRollupRule {
    override fun check(item: TrailerRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRollupQuantityRangeRule(var min: Int, var max: Int) : TrailerRollupRule {
    override fun check(item: TrailerRollup): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a trailer rollup. */
data class TrailerRollupValidator(var rules: MutableList<TrailerRollupRule>) {
    fun validate(item: TrailerRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerRollupValidator(): TrailerRollupValidator {
    return TrailerRollupValidator(
        mutableListOf<TrailerRollupRule>(
            TrailerRollupLaneLengthRule(1000),
            TrailerRollupAllowedCarrierRule(mutableListOf<String>("weighted")),
            TrailerRollupCodeLengthRule(32),
            TrailerRollupNameLengthRule(80),
            TrailerRollupQuantityRangeRule(32, 80),
        ),
    )
}
