package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.text.normalize

/** A rule a near miss audit must pass. Check returns its problems, or none. */
interface NearMissAuditRule {
    fun check(item: NearMissAudit): MutableList<String>
}

data class NearMissAuditAllowedCodeRule(var allowed: MutableList<String>) : NearMissAuditRule {
    override fun check(item: NearMissAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAuditCountryLengthRule(var maxLength: Int) : NearMissAuditRule {
    override fun check(item: NearMissAudit): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAuditNameLengthRule(var maxLength: Int) : NearMissAuditRule {
    override fun check(item: NearMissAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissAuditRequiredNameRule(var message: String) : NearMissAuditRule {
    override fun check(item: NearMissAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissAuditThresholdRangeRule(var min: Double, var max: Double) : NearMissAuditRule {
    override fun check(item: NearMissAudit): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a near miss audit. */
data class NearMissAuditValidator(var rules: MutableList<NearMissAuditRule>) {
    fun validate(item: NearMissAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissAuditValidator(): NearMissAuditValidator {
    return NearMissAuditValidator(
        mutableListOf<NearMissAuditRule>(
            NearMissAuditAllowedCodeRule(mutableListOf<String>("lenient", "tiered")),
            NearMissAuditCountryLengthRule(3600),
            NearMissAuditNameLengthRule(32),
            NearMissAuditRequiredNameRule("near miss audit needs a value"),
            NearMissAuditThresholdRangeRule(5.0, 452.9),
        ),
    )
}
