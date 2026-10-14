package com.acme.transport.linehaul.relay.schedule

/** A rule a relay baseline must pass. Check returns its problems, or none. */
interface RelayBaselineRule {
    fun check(item: RelayBaseline): MutableList<String>
}

data class RelayBaselineSummaryLengthRule(var maxLength: Int) : RelayBaselineRule {
    override fun check(item: RelayBaseline): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayBaselineRequiredTitleRule(var message: String) : RelayBaselineRule {
    override fun check(item: RelayBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayBaselineStopCountRangeRule(var min: Int, var max: Int) : RelayBaselineRule {
    override fun check(item: RelayBaseline): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay baseline. */
data class RelayBaselineValidator(var rules: MutableList<RelayBaselineRule>) {
    fun validate(item: RelayBaseline): MutableList<String> {
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

    fun requireValid(item: RelayBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayBaselineValidator(): RelayBaselineValidator {
    return RelayBaselineValidator(
        mutableListOf<RelayBaselineRule>(
            RelayBaselineSummaryLengthRule(1000),
            RelayBaselineRequiredTitleRule("relay baseline needs a value"),
            RelayBaselineStopCountRangeRule(120, 255),
        ),
    )
}
