package com.acme.transport.safety.scorecard.rule

/** A rule a primary scorecard window must pass. Check returns its problems, or none. */
interface PrimaryScorecardWindowRule {
    fun check(item: PrimaryScorecardWindow): MutableList<String>
}

data class PrimaryScorecardWindowCodeLengthRule(var maxLength: Int) : PrimaryScorecardWindowRule {
    override fun check(item: PrimaryScorecardWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardWindowRequiredLabelRule(var message: String) : PrimaryScorecardWindowRule {
    override fun check(item: PrimaryScorecardWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardWindowStopCountRangeRule(var min: Int, var max: Int) : PrimaryScorecardWindowRule {
    override fun check(item: PrimaryScorecardWindow): MutableList<String> {
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

/** Runs every rule of a primary scorecard window. */
data class PrimaryScorecardWindowValidator(var rules: MutableList<PrimaryScorecardWindowRule>) {
    fun validate(item: PrimaryScorecardWindow): MutableList<String> {
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

    fun requireValid(item: PrimaryScorecardWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryScorecardWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryScorecardWindowValidator(): PrimaryScorecardWindowValidator {
    return PrimaryScorecardWindowValidator(
        mutableListOf<PrimaryScorecardWindowRule>(
            PrimaryScorecardWindowCodeLengthRule(64),
            PrimaryScorecardWindowRequiredLabelRule("primary scorecard window needs this field"),
            PrimaryScorecardWindowStopCountRangeRule(80, 255),
        ),
    )
}
