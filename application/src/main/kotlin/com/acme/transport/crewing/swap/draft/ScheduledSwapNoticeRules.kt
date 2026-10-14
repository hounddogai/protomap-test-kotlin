package com.acme.transport.crewing.swap.draft

/** A rule a scheduled swap notice must pass. Check returns its problems, or none. */
interface ScheduledSwapNoticeRule {
    fun check(item: ScheduledSwapNotice): MutableList<String>
}

data class ScheduledSwapNoticeContactEmailLengthRule(var maxLength: Int) : ScheduledSwapNoticeRule {
    override fun check(item: ScheduledSwapNotice): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapNoticeCarrierLengthRule(var maxLength: Int) : ScheduledSwapNoticeRule {
    override fun check(item: ScheduledSwapNotice): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapNoticeFloorValueRangeRule(var min: Double, var max: Double) : ScheduledSwapNoticeRule {
    override fun check(item: ScheduledSwapNotice): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled swap notice. */
data class ScheduledSwapNoticeValidator(var rules: MutableList<ScheduledSwapNoticeRule>) {
    fun validate(item: ScheduledSwapNotice): MutableList<String> {
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

    fun requireValid(item: ScheduledSwapNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledSwapNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledSwapNoticeValidator(): ScheduledSwapNoticeValidator {
    return ScheduledSwapNoticeValidator(
        mutableListOf<ScheduledSwapNoticeRule>(
            ScheduledSwapNoticeContactEmailLengthRule(86400),
            ScheduledSwapNoticeCarrierLengthRule(120),
            ScheduledSwapNoticeFloorValueRangeRule(2.5, 4998.8),
        ),
    )
}
