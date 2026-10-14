package com.acme.transport.terminal.dwell.milestone

/** A rule a dwell summary must pass. Check returns its problems, or none. */
interface DwellSummaryRule {
    fun check(item: DwellSummary): MutableList<String>
}

data class DwellSummaryRequiredCodeRule(var message: String) : DwellSummaryRule {
    override fun check(item: DwellSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellSummaryThresholdRangeRule(var min: Double, var max: Double) : DwellSummaryRule {
    override fun check(item: DwellSummary): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellSummaryLabelLengthRule(var maxLength: Int) : DwellSummaryRule {
    override fun check(item: DwellSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellSummaryAttemptsRangeRule(var min: Int, var max: Int) : DwellSummaryRule {
    override fun check(item: DwellSummary): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell summary. */
data class DwellSummaryValidator(var rules: MutableList<DwellSummaryRule>) {
    fun validate(item: DwellSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellSummaryValidator(): DwellSummaryValidator {
    return DwellSummaryValidator(
        mutableListOf<DwellSummaryRule>(
            DwellSummaryRequiredCodeRule("dwell summary needs a value"),
            DwellSummaryThresholdRangeRule(4.0, 3284.6),
            DwellSummaryLabelLengthRule(80),
            DwellSummaryAttemptsRangeRule(64, 86400),
        ),
    )
}
