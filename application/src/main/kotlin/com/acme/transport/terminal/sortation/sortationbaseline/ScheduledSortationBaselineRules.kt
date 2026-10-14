package com.acme.transport.terminal.sortation.sortationbaseline

/** A rule a scheduled sortation baseline must pass. Check returns its problems, or none. */
interface ScheduledSortationBaselineRule {
    fun check(item: ScheduledSortationBaseline): MutableList<String>
}

data class ScheduledSortationBaselineRequiredCarrierRule(var message: String) : ScheduledSortationBaselineRule {
    override fun check(item: ScheduledSortationBaseline): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationBaselineRequiredCodeRule(var message: String) : ScheduledSortationBaselineRule {
    override fun check(item: ScheduledSortationBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationBaselineCodePrefixRule(var prefix: String) : ScheduledSortationBaselineRule {
    override fun check(item: ScheduledSortationBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled sortation baseline. */
data class ScheduledSortationBaselineValidator(var rules: MutableList<ScheduledSortationBaselineRule>) {
    fun validate(item: ScheduledSortationBaseline): MutableList<String> {
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

    fun requireValid(item: ScheduledSortationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledSortationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledSortationBaselineValidator(): ScheduledSortationBaselineValidator {
    return ScheduledSortationBaselineValidator(
        mutableListOf<ScheduledSortationBaselineRule>(
            ScheduledSortationBaselineRequiredCarrierRule("scheduled sortation baseline needs an entry"),
            ScheduledSortationBaselineRequiredCodeRule("scheduled sortation baseline needs a value"),
            ScheduledSortationBaselineCodePrefixRule("X"),
        ),
    )
}
