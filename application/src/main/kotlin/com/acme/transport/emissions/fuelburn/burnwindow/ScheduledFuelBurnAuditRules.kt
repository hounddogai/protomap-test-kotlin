package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.text.normalize

/** A rule a scheduled fuel burn audit must pass. Check returns its problems, or none. */
interface ScheduledFuelBurnAuditRule {
    fun check(item: ScheduledFuelBurnAudit): MutableList<String>
}

data class ScheduledFuelBurnAuditCodeLengthRule(var maxLength: Int) : ScheduledFuelBurnAuditRule {
    override fun check(item: ScheduledFuelBurnAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnAuditLabelLengthRule(var maxLength: Int) : ScheduledFuelBurnAuditRule {
    override fun check(item: ScheduledFuelBurnAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnAuditAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledFuelBurnAuditRule {
    override fun check(item: ScheduledFuelBurnAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnAuditCodePrefixRule(var prefix: String) : ScheduledFuelBurnAuditRule {
    override fun check(item: ScheduledFuelBurnAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnAuditMaximumRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledFuelBurnAuditRule {
    override fun check(item: ScheduledFuelBurnAudit): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled fuel burn audit. */
data class ScheduledFuelBurnAuditValidator(var rules: MutableList<ScheduledFuelBurnAuditRule>) {
    fun validate(item: ScheduledFuelBurnAudit): MutableList<String> {
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

    fun requireValid(item: ScheduledFuelBurnAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFuelBurnAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFuelBurnAuditValidator(): ScheduledFuelBurnAuditValidator {
    return ScheduledFuelBurnAuditValidator(
        mutableListOf<ScheduledFuelBurnAuditRule>(
            ScheduledFuelBurnAuditCodeLengthRule(3600),
            ScheduledFuelBurnAuditLabelLengthRule(120),
            ScheduledFuelBurnAuditAllowedCodeRule(mutableListOf<String>("scheduled")),
            ScheduledFuelBurnAuditCodePrefixRule("Q"),
            ScheduledFuelBurnAuditMaximumRangeRule(8.2, 224.4),
        ),
    )
}
