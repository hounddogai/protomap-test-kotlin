package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.kit.text.normalize

/** A rule a appointment estimate must pass. Check returns its problems, or none. */
interface AppointmentEstimateRule {
    fun check(item: AppointmentEstimate): MutableList<String>
}

data class AppointmentEstimateAllowedLabelRule(var allowed: MutableList<String>) : AppointmentEstimateRule {
    override fun check(item: AppointmentEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentEstimateLabelLengthRule(var maxLength: Int) : AppointmentEstimateRule {
    override fun check(item: AppointmentEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentEstimateWindowDaysRangeRule(var min: Int, var max: Int) : AppointmentEstimateRule {
    override fun check(item: AppointmentEstimate): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class AppointmentEstimateCodeLengthRule(var maxLength: Int) : AppointmentEstimateRule {
    override fun check(item: AppointmentEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentEstimateChecksumLengthRule(var maxLength: Int) : AppointmentEstimateRule {
    override fun check(item: AppointmentEstimate): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentEstimateRequiredChecksumRule(var message: String) : AppointmentEstimateRule {
    override fun check(item: AppointmentEstimate): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment estimate. */
data class AppointmentEstimateValidator(var rules: MutableList<AppointmentEstimateRule>) {
    fun validate(item: AppointmentEstimate): MutableList<String> {
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

    fun requireValid(item: AppointmentEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentEstimateValidator(): AppointmentEstimateValidator {
    return AppointmentEstimateValidator(
        mutableListOf<AppointmentEstimateRule>(
            AppointmentEstimateAllowedLabelRule(mutableListOf<String>("default", "regional")),
            AppointmentEstimateLabelLengthRule(16),
            AppointmentEstimateWindowDaysRangeRule(16, 86400),
            AppointmentEstimateCodeLengthRule(80),
            AppointmentEstimateChecksumLengthRule(255),
            AppointmentEstimateRequiredChecksumRule("appointment estimate needs this field"),
        ),
    )
}
