package com.acme.transport.terminal.dwell.notice

/** A rule a dwell score must pass. Check returns its problems, or none. */
interface DwellScoreRule {
    fun check(item: DwellScore): MutableList<String>
}

data class DwellScoreRequiredLabelRule(var message: String) : DwellScoreRule {
    override fun check(item: DwellScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellScorePriorityRangeRule(var min: Int, var max: Int) : DwellScoreRule {
    override fun check(item: DwellScore): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class DwellScoreCodePrefixRule(var prefix: String) : DwellScoreRule {
    override fun check(item: DwellScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell score. */
data class DwellScoreValidator(var rules: MutableList<DwellScoreRule>) {
    fun validate(item: DwellScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellScoreValidator(): DwellScoreValidator {
    return DwellScoreValidator(
        mutableListOf<DwellScoreRule>(
            DwellScoreRequiredLabelRule("dwell score needs this field"),
            DwellScorePriorityRangeRule(64, 1000),
            DwellScoreCodePrefixRule("Q"),
        ),
    )
}
