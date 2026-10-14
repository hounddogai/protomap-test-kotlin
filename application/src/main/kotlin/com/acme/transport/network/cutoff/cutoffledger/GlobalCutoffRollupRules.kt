package com.acme.transport.network.cutoff.cutoffledger

/** A rule a global cutoff rollup must pass. Check returns its problems, or none. */
interface GlobalCutoffRollupRule {
    fun check(item: GlobalCutoffRollup): MutableList<String>
}

data class GlobalCutoffRollupBudgetRangeRule(var min: Double, var max: Double) : GlobalCutoffRollupRule {
    override fun check(item: GlobalCutoffRollup): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class GlobalCutoffRollupTitleLengthRule(var maxLength: Int) : GlobalCutoffRollupRule {
    override fun check(item: GlobalCutoffRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCutoffRollupRequiredCodeRule(var message: String) : GlobalCutoffRollupRule {
    override fun check(item: GlobalCutoffRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCutoffRollupRequiredLaneRule(var message: String) : GlobalCutoffRollupRule {
    override fun check(item: GlobalCutoffRollup): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cutoff rollup. */
data class GlobalCutoffRollupValidator(var rules: MutableList<GlobalCutoffRollupRule>) {
    fun validate(item: GlobalCutoffRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCutoffRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCutoffRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCutoffRollupValidator(): GlobalCutoffRollupValidator {
    return GlobalCutoffRollupValidator(
        mutableListOf<GlobalCutoffRollupRule>(
            GlobalCutoffRollupBudgetRangeRule(0.4, 1332.9),
            GlobalCutoffRollupTitleLengthRule(3600),
            GlobalCutoffRollupRequiredCodeRule("global cutoff rollup needs an entry"),
            GlobalCutoffRollupRequiredLaneRule("global cutoff rollup needs a value"),
        ),
    )
}
