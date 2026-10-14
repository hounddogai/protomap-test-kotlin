package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.text.normalize

/** A rule a legacy attempt audit must pass. Check returns its problems, or none. */
interface LegacyAttemptAuditRule {
    fun check(item: LegacyAttemptAudit): MutableList<String>
}

data class LegacyAttemptAuditCodeLengthRule(var maxLength: Int) : LegacyAttemptAuditRule {
    override fun check(item: LegacyAttemptAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAttemptAuditCodePrefixRule(var prefix: String) : LegacyAttemptAuditRule {
    override fun check(item: LegacyAttemptAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAttemptAuditRequiredCarrierRule(var message: String) : LegacyAttemptAuditRule {
    override fun check(item: LegacyAttemptAudit): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAttemptAuditAllowedNameRule(var allowed: MutableList<String>) : LegacyAttemptAuditRule {
    override fun check(item: LegacyAttemptAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAttemptAuditCarrierLengthRule(var maxLength: Int) : LegacyAttemptAuditRule {
    override fun check(item: LegacyAttemptAudit): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAttemptAuditFuelLitersRangeRule(var min: Double, var max: Double) : LegacyAttemptAuditRule {
    override fun check(item: LegacyAttemptAudit): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy attempt audit. */
data class LegacyAttemptAuditValidator(var rules: MutableList<LegacyAttemptAuditRule>) {
    fun validate(item: LegacyAttemptAudit): MutableList<String> {
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

    fun requireValid(item: LegacyAttemptAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAttemptAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAttemptAuditValidator(): LegacyAttemptAuditValidator {
    return LegacyAttemptAuditValidator(
        mutableListOf<LegacyAttemptAuditRule>(
            LegacyAttemptAuditCodeLengthRule(32),
            LegacyAttemptAuditCodePrefixRule("X"),
            LegacyAttemptAuditRequiredCarrierRule("legacy attempt audit needs this field"),
            LegacyAttemptAuditAllowedNameRule(mutableListOf<String>("rolling", "legacy", "weighted")),
            LegacyAttemptAuditCarrierLengthRule(3600),
            LegacyAttemptAuditFuelLitersRangeRule(8.7, 3208.4),
        ),
    )
}
