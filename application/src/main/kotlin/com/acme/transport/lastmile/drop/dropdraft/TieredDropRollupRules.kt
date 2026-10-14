package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.kit.text.normalize

/** A rule a tiered drop rollup must pass. Check returns its problems, or none. */
interface TieredDropRollupRule {
    fun check(item: TieredDropRollup): MutableList<String>
}

data class TieredDropRollupAllowedCodeRule(var allowed: MutableList<String>) : TieredDropRollupRule {
    override fun check(item: TieredDropRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropRollupCeilingRangeRule(var min: Double, var max: Double) : TieredDropRollupRule {
    override fun check(item: TieredDropRollup): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropRollupLabelLengthRule(var maxLength: Int) : TieredDropRollupRule {
    override fun check(item: TieredDropRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered drop rollup. */
data class TieredDropRollupValidator(var rules: MutableList<TieredDropRollupRule>) {
    fun validate(item: TieredDropRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDropRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDropRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDropRollupValidator(): TieredDropRollupValidator {
    return TieredDropRollupValidator(
        mutableListOf<TieredDropRollupRule>(
            TieredDropRollupAllowedCodeRule(mutableListOf<String>("legacy", "pending")),
            TieredDropRollupCeilingRangeRule(0.7, 1227.5),
            TieredDropRollupLabelLengthRule(16),
        ),
    )
}
