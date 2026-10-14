package com.acme.transport.freightpay.ratecard.milestone

/** A rule a scheduled rate card rollup must pass. Check returns its problems, or none. */
interface ScheduledRateCardRollupRule {
    fun check(item: ScheduledRateCardRollup): MutableList<String>
}

data class ScheduledRateCardRollupRequiredRegionRule(var message: String) : ScheduledRateCardRollupRule {
    override fun check(item: ScheduledRateCardRollup): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRateCardRollupCodeLengthRule(var maxLength: Int) : ScheduledRateCardRollupRule {
    override fun check(item: ScheduledRateCardRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRateCardRollupRequiredCodeRule(var message: String) : ScheduledRateCardRollupRule {
    override fun check(item: ScheduledRateCardRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled rate card rollup. */
data class ScheduledRateCardRollupValidator(var rules: MutableList<ScheduledRateCardRollupRule>) {
    fun validate(item: ScheduledRateCardRollup): MutableList<String> {
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

    fun requireValid(item: ScheduledRateCardRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRateCardRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRateCardRollupValidator(): ScheduledRateCardRollupValidator {
    return ScheduledRateCardRollupValidator(
        mutableListOf<ScheduledRateCardRollupRule>(
            ScheduledRateCardRollupRequiredRegionRule("scheduled rate card rollup needs this field"),
            ScheduledRateCardRollupCodeLengthRule(16),
            ScheduledRateCardRollupRequiredCodeRule("scheduled rate card rollup needs a value"),
        ),
    )
}
