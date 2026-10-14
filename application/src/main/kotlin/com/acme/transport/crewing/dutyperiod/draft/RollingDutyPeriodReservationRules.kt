package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.normalize

/** A rule a rolling duty period reservation must pass. Check returns its problems, or none. */
interface RollingDutyPeriodReservationRule {
    fun check(item: RollingDutyPeriodReservation): MutableList<String>
}

data class RollingDutyPeriodReservationRequiredLabelRule(
    var message: String,
) : RollingDutyPeriodReservationRule {
    override fun check(item: RollingDutyPeriodReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodReservationCodePrefixRule(var prefix: String) : RollingDutyPeriodReservationRule {
    override fun check(item: RollingDutyPeriodReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodReservationLabelLengthRule(var maxLength: Int) : RollingDutyPeriodReservationRule {
    override fun check(item: RollingDutyPeriodReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodReservationAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : RollingDutyPeriodReservationRule {
    override fun check(item: RollingDutyPeriodReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodReservationSummaryLengthRule(
    var maxLength: Int,
) : RollingDutyPeriodReservationRule {
    override fun check(item: RollingDutyPeriodReservation): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodReservationNotesLengthRule(var maxLength: Int) : RollingDutyPeriodReservationRule {
    override fun check(item: RollingDutyPeriodReservation): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling duty period reservation. */
data class RollingDutyPeriodReservationValidator(var rules: MutableList<RollingDutyPeriodReservationRule>) {
    fun validate(item: RollingDutyPeriodReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDutyPeriodReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDutyPeriodReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDutyPeriodReservationValidator(): RollingDutyPeriodReservationValidator {
    return RollingDutyPeriodReservationValidator(
        mutableListOf<RollingDutyPeriodReservationRule>(
            RollingDutyPeriodReservationRequiredLabelRule("rolling duty period reservation needs this field"),
            RollingDutyPeriodReservationCodePrefixRule("ACM"),
            RollingDutyPeriodReservationLabelLengthRule(120),
            RollingDutyPeriodReservationAllowedDescriptionRule(mutableListOf<String>("manual", "strict")),
            RollingDutyPeriodReservationSummaryLengthRule(16),
            RollingDutyPeriodReservationNotesLengthRule(1000),
        ),
    )
}
