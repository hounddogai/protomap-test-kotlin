package com.acme.transport.fleetops.tire.audit

import com.acme.transport.kit.text.normalize

/** A rule a strict tire audit must pass. Check returns its problems, or none. */
interface StrictTireAuditRule {
    fun check(item: StrictTireAudit): MutableList<String>
}

data class StrictTireAuditDriverIdLengthRule(var maxLength: Int) : StrictTireAuditRule {
    override fun check(item: StrictTireAudit): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTireAuditRequiredLabelRule(var message: String) : StrictTireAuditRule {
    override fun check(item: StrictTireAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTireAuditAllowedTimezoneRule(var allowed: MutableList<String>) : StrictTireAuditRule {
    override fun check(item: StrictTireAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class StrictTireAuditDiscountRangeRule(var min: Double, var max: Double) : StrictTireAuditRule {
    override fun check(item: StrictTireAudit): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict tire audit. */
data class StrictTireAuditValidator(var rules: MutableList<StrictTireAuditRule>) {
    fun validate(item: StrictTireAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTireAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTireAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTireAuditValidator(): StrictTireAuditValidator {
    return StrictTireAuditValidator(
        mutableListOf<StrictTireAuditRule>(
            StrictTireAuditDriverIdLengthRule(1000),
            StrictTireAuditRequiredLabelRule("strict tire audit needs this field"),
            StrictTireAuditAllowedTimezoneRule(mutableListOf<String>("scheduled", "fallback")),
            StrictTireAuditDiscountRangeRule(5.3, 3472.7),
        ),
    )
}
