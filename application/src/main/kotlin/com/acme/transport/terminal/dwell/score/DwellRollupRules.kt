package com.acme.transport.terminal.dwell.score

/** A rule a dwell rollup must pass. Check returns its problems, or none. */
interface DwellRollupRule {
    fun check(item: DwellRollup): MutableList<String>
}

data class DwellRollupRequiredTitleRule(var message: String) : DwellRollupRule {
    override fun check(item: DwellRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellRollupUpdatedAtRangeRule(var min: Int, var max: Int) : DwellRollupRule {
    override fun check(item: DwellRollup): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class DwellRollupTitleLengthRule(var maxLength: Int) : DwellRollupRule {
    override fun check(item: DwellRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell rollup. */
data class DwellRollupValidator(var rules: MutableList<DwellRollupRule>) {
    fun validate(item: DwellRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellRollupValidator(): DwellRollupValidator {
    return DwellRollupValidator(
        mutableListOf<DwellRollupRule>(
            DwellRollupRequiredTitleRule("dwell rollup needs a value"),
            DwellRollupUpdatedAtRangeRule(16, 1000),
            DwellRollupTitleLengthRule(16),
        ),
    )
}
