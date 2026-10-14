package com.acme.transport.terminal.sortation.sortationadjustment

/** A rule a scheduled sortation adjustment must pass. Check returns its problems, or none. */
interface ScheduledSortationAdjustmentRule {
    fun check(item: ScheduledSortationAdjustment): MutableList<String>
}

data class ScheduledSortationAdjustmentRequiredTitleRule(
    var message: String,
) : ScheduledSortationAdjustmentRule {
    override fun check(item: ScheduledSortationAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationAdjustmentCodePrefixRule(var prefix: String) : ScheduledSortationAdjustmentRule {
    override fun check(item: ScheduledSortationAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSortationAdjustmentCurrencyLengthRule(
    var maxLength: Int,
) : ScheduledSortationAdjustmentRule {
    override fun check(item: ScheduledSortationAdjustment): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled sortation adjustment. */
data class ScheduledSortationAdjustmentValidator(var rules: MutableList<ScheduledSortationAdjustmentRule>) {
    fun validate(item: ScheduledSortationAdjustment): MutableList<String> {
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

    fun requireValid(item: ScheduledSortationAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledSortationAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledSortationAdjustmentValidator(): ScheduledSortationAdjustmentValidator {
    return ScheduledSortationAdjustmentValidator(
        mutableListOf<ScheduledSortationAdjustmentRule>(
            ScheduledSortationAdjustmentRequiredTitleRule("scheduled sortation adjustment needs this field"),
            ScheduledSortationAdjustmentCodePrefixRule("REF"),
            ScheduledSortationAdjustmentCurrencyLengthRule(64),
        ),
    )
}
