package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.text.normalize

/** A rule a adaptive lease return schedule must pass. Check returns its problems, or none. */
interface AdaptiveLeaseReturnScheduleRule {
    fun check(item: AdaptiveLeaseReturnSchedule): MutableList<String>
}

data class AdaptiveLeaseReturnScheduleAllowedLabelRule(
    var allowed: MutableList<String>,
) : AdaptiveLeaseReturnScheduleRule {
    override fun check(item: AdaptiveLeaseReturnSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnScheduleRequiredLabelRule(var message: String) : AdaptiveLeaseReturnScheduleRule {
    override fun check(item: AdaptiveLeaseReturnSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnScheduleLabelLengthRule(var maxLength: Int) : AdaptiveLeaseReturnScheduleRule {
    override fun check(item: AdaptiveLeaseReturnSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnScheduleSummaryLengthRule(var maxLength: Int) : AdaptiveLeaseReturnScheduleRule {
    override fun check(item: AdaptiveLeaseReturnSchedule): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lease return schedule. */
data class AdaptiveLeaseReturnScheduleValidator(var rules: MutableList<AdaptiveLeaseReturnScheduleRule>) {
    fun validate(item: AdaptiveLeaseReturnSchedule): MutableList<String> {
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

    fun requireValid(item: AdaptiveLeaseReturnSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLeaseReturnSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLeaseReturnScheduleValidator(): AdaptiveLeaseReturnScheduleValidator {
    return AdaptiveLeaseReturnScheduleValidator(
        mutableListOf<AdaptiveLeaseReturnScheduleRule>(
            AdaptiveLeaseReturnScheduleAllowedLabelRule(mutableListOf<String>("default")),
            AdaptiveLeaseReturnScheduleRequiredLabelRule("adaptive lease return schedule needs this field"),
            AdaptiveLeaseReturnScheduleLabelLengthRule(16),
            AdaptiveLeaseReturnScheduleSummaryLengthRule(3600),
        ),
    )
}
