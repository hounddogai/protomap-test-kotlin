package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.text.normalize

/** A rule a seasonal appointment override must pass. Check returns its problems, or none. */
interface SeasonalAppointmentOverrideRule {
    fun check(item: SeasonalAppointmentOverride): MutableList<String>
}

data class SeasonalAppointmentOverrideRequiredOwnerIdRule(
    var message: String,
) : SeasonalAppointmentOverrideRule {
    override fun check(item: SeasonalAppointmentOverride): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAppointmentOverrideRequiredTrailerIdRule(
    var message: String,
) : SeasonalAppointmentOverrideRule {
    override fun check(item: SeasonalAppointmentOverride): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAppointmentOverrideAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : SeasonalAppointmentOverrideRule {
    override fun check(item: SeasonalAppointmentOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAppointmentOverrideLabelLengthRule(var maxLength: Int) : SeasonalAppointmentOverrideRule {
    override fun check(item: SeasonalAppointmentOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAppointmentOverrideOwnerIdLengthRule(var maxLength: Int) : SeasonalAppointmentOverrideRule {
    override fun check(item: SeasonalAppointmentOverride): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal appointment override. */
data class SeasonalAppointmentOverrideValidator(var rules: MutableList<SeasonalAppointmentOverrideRule>) {
    fun validate(item: SeasonalAppointmentOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAppointmentOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAppointmentOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAppointmentOverrideValidator(): SeasonalAppointmentOverrideValidator {
    return SeasonalAppointmentOverrideValidator(
        mutableListOf<SeasonalAppointmentOverrideRule>(
            SeasonalAppointmentOverrideRequiredOwnerIdRule("seasonal appointment override needs this field"),
            SeasonalAppointmentOverrideRequiredTrailerIdRule("seasonal appointment override needs an entry"),
            SeasonalAppointmentOverrideAllowedOwnerIdRule(mutableListOf<String>()),
            SeasonalAppointmentOverrideLabelLengthRule(3600),
            SeasonalAppointmentOverrideOwnerIdLengthRule(32),
        ),
    )
}
