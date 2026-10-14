package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.text.normalize

/** A rule a capped appointment allocation must pass. Check returns its problems, or none. */
interface CappedAppointmentAllocationRule {
    fun check(item: CappedAppointmentAllocation): MutableList<String>
}

data class CappedAppointmentAllocationRequiredTimezoneRule(
    var message: String,
) : CappedAppointmentAllocationRule {
    override fun check(item: CappedAppointmentAllocation): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAppointmentAllocationAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : CappedAppointmentAllocationRule {
    override fun check(item: CappedAppointmentAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class CappedAppointmentAllocationCodePrefixRule(var prefix: String) : CappedAppointmentAllocationRule {
    override fun check(item: CappedAppointmentAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped appointment allocation. */
data class CappedAppointmentAllocationValidator(var rules: MutableList<CappedAppointmentAllocationRule>) {
    fun validate(item: CappedAppointmentAllocation): MutableList<String> {
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

    fun requireValid(item: CappedAppointmentAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAppointmentAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAppointmentAllocationValidator(): CappedAppointmentAllocationValidator {
    return CappedAppointmentAllocationValidator(
        mutableListOf<CappedAppointmentAllocationRule>(
            CappedAppointmentAllocationRequiredTimezoneRule("capped appointment allocation needs an entry"),
            CappedAppointmentAllocationAllowedTimezoneRule(mutableListOf<String>("legacy", "global")),
            CappedAppointmentAllocationCodePrefixRule("Q"),
        ),
    )
}
