package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import com.acme.transport.kit.text.normalize

/** A rule a appointment proposal must pass. Check returns its problems, or none. */
interface AppointmentProposalRule {
    fun check(item: AppointmentProposal): MutableList<String>
}

data class AppointmentProposalStopCountRangeRule(var min: Int, var max: Int) : AppointmentProposalRule {
    override fun check(item: AppointmentProposal): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class AppointmentProposalChecksumLengthRule(var maxLength: Int) : AppointmentProposalRule {
    override fun check(item: AppointmentProposal): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentProposalRequiredLabelRule(var message: String) : AppointmentProposalRule {
    override fun check(item: AppointmentProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentProposalRequiredChecksumRule(var message: String) : AppointmentProposalRule {
    override fun check(item: AppointmentProposal): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentProposalAllowedCodeRule(var allowed: MutableList<String>) : AppointmentProposalRule {
    override fun check(item: AppointmentProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentProposalLabelLengthRule(var maxLength: Int) : AppointmentProposalRule {
    override fun check(item: AppointmentProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment proposal. */
data class AppointmentProposalValidator(var rules: MutableList<AppointmentProposalRule>) {
    fun validate(item: AppointmentProposal): MutableList<String> {
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

    fun requireValid(item: AppointmentProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentProposalValidator(): AppointmentProposalValidator {
    return AppointmentProposalValidator(
        mutableListOf<AppointmentProposalRule>(
            AppointmentProposalStopCountRangeRule(120, 1000),
            AppointmentProposalChecksumLengthRule(120),
            AppointmentProposalRequiredLabelRule("appointment proposal needs an entry"),
            AppointmentProposalRequiredChecksumRule("appointment proposal needs an entry"),
            AppointmentProposalAllowedCodeRule(mutableListOf<String>("adaptive", "default")),
            AppointmentProposalLabelLengthRule(32),
        ),
    )
}
