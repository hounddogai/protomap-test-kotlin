package com.acme.transport.terminal.appointment.entry

import com.acme.transport.kit.text.normalize

/** A rule a fallback appointment entry must pass. Check returns its problems, or none. */
interface FallbackAppointmentEntryRule {
    fun check(item: FallbackAppointmentEntry): MutableList<String>
}

data class FallbackAppointmentEntryTitleLengthRule(var maxLength: Int) : FallbackAppointmentEntryRule {
    override fun check(item: FallbackAppointmentEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentEntryAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : FallbackAppointmentEntryRule {
    override fun check(item: FallbackAppointmentEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentEntryRequiredChecksumRule(var message: String) : FallbackAppointmentEntryRule {
    override fun check(item: FallbackAppointmentEntry): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentEntryPriorityRangeRule(
    var min: Int,
    var max: Int,
) : FallbackAppointmentEntryRule {
    override fun check(item: FallbackAppointmentEntry): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class FallbackAppointmentEntryRequiredTitleRule(var message: String) : FallbackAppointmentEntryRule {
    override fun check(item: FallbackAppointmentEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentEntryRequiredCodeRule(var message: String) : FallbackAppointmentEntryRule {
    override fun check(item: FallbackAppointmentEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback appointment entry. */
data class FallbackAppointmentEntryValidator(var rules: MutableList<FallbackAppointmentEntryRule>) {
    fun validate(item: FallbackAppointmentEntry): MutableList<String> {
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

    fun requireValid(item: FallbackAppointmentEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAppointmentEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAppointmentEntryValidator(): FallbackAppointmentEntryValidator {
    return FallbackAppointmentEntryValidator(
        mutableListOf<FallbackAppointmentEntryRule>(
            FallbackAppointmentEntryTitleLengthRule(1000),
            FallbackAppointmentEntryAllowedDescriptionRule(mutableListOf<String>("rolling", "pending")),
            FallbackAppointmentEntryRequiredChecksumRule("fallback appointment entry needs an entry"),
            FallbackAppointmentEntryPriorityRangeRule(120, 1000),
            FallbackAppointmentEntryRequiredTitleRule("fallback appointment entry needs this field"),
            FallbackAppointmentEntryRequiredCodeRule("fallback appointment entry needs this field"),
        ),
    )
}
