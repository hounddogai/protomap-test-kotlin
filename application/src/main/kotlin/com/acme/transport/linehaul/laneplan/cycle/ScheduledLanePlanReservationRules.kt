package com.acme.transport.linehaul.laneplan.cycle

/** A rule a scheduled lane plan reservation must pass. Check returns its problems, or none. */
interface ScheduledLanePlanReservationRule {
    fun check(item: ScheduledLanePlanReservation): MutableList<String>
}

data class ScheduledLanePlanReservationRatioRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledLanePlanReservationRule {
    override fun check(item: ScheduledLanePlanReservation): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class ScheduledLanePlanReservationRequiredLabelRule(
    var message: String,
) : ScheduledLanePlanReservationRule {
    override fun check(item: ScheduledLanePlanReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanReservationPalletsRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledLanePlanReservationRule {
    override fun check(item: ScheduledLanePlanReservation): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class ScheduledLanePlanReservationLabelLengthRule(var maxLength: Int) : ScheduledLanePlanReservationRule {
    override fun check(item: ScheduledLanePlanReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanReservationCodePrefixRule(var prefix: String) : ScheduledLanePlanReservationRule {
    override fun check(item: ScheduledLanePlanReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanReservationScoreRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledLanePlanReservationRule {
    override fun check(item: ScheduledLanePlanReservation): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled lane plan reservation. */
data class ScheduledLanePlanReservationValidator(var rules: MutableList<ScheduledLanePlanReservationRule>) {
    fun validate(item: ScheduledLanePlanReservation): MutableList<String> {
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

    fun requireValid(item: ScheduledLanePlanReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLanePlanReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLanePlanReservationValidator(): ScheduledLanePlanReservationValidator {
    return ScheduledLanePlanReservationValidator(
        mutableListOf<ScheduledLanePlanReservationRule>(
            ScheduledLanePlanReservationRatioRangeRule(1.1, 4293.8),
            ScheduledLanePlanReservationRequiredLabelRule("scheduled lane plan reservation needs this field"),
            ScheduledLanePlanReservationPalletsRangeRule(64, 64),
            ScheduledLanePlanReservationLabelLengthRule(255),
            ScheduledLanePlanReservationCodePrefixRule("Q"),
            ScheduledLanePlanReservationScoreRangeRule(4.5, 4008.6),
        ),
    )
}
