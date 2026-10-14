package com.acme.transport.crewing.swap.agreement

import com.acme.transport.kit.text.normalize

/** A rule a seasonal swap rollup must pass. Check returns its problems, or none. */
interface SeasonalSwapRollupRule {
    fun check(item: SeasonalSwapRollup): MutableList<String>
}

data class SeasonalSwapRollupDockLengthRule(var maxLength: Int) : SeasonalSwapRollupRule {
    override fun check(item: SeasonalSwapRollup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSwapRollupAllowedTitleRule(var allowed: MutableList<String>) : SeasonalSwapRollupRule {
    override fun check(item: SeasonalSwapRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSwapRollupCodeLengthRule(var maxLength: Int) : SeasonalSwapRollupRule {
    override fun check(item: SeasonalSwapRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal swap rollup. */
data class SeasonalSwapRollupValidator(var rules: MutableList<SeasonalSwapRollupRule>) {
    fun validate(item: SeasonalSwapRollup): MutableList<String> {
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

    fun requireValid(item: SeasonalSwapRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalSwapRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalSwapRollupValidator(): SeasonalSwapRollupValidator {
    return SeasonalSwapRollupValidator(
        mutableListOf<SeasonalSwapRollupRule>(
            SeasonalSwapRollupDockLengthRule(64),
            SeasonalSwapRollupAllowedTitleRule(mutableListOf<String>("manual", "global", "rolling")),
            SeasonalSwapRollupCodeLengthRule(3600),
        ),
    )
}
