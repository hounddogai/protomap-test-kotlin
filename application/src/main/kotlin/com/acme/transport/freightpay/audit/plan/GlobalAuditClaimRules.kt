package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.text.normalize

/** A rule a global audit claim must pass. Check returns its problems, or none. */
interface GlobalAuditClaimRule {
    fun check(item: GlobalAuditClaim): MutableList<String>
}

data class GlobalAuditClaimEtaMinutesRangeRule(var min: Int, var max: Int) : GlobalAuditClaimRule {
    override fun check(item: GlobalAuditClaim): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditClaimAttemptsRangeRule(var min: Int, var max: Int) : GlobalAuditClaimRule {
    override fun check(item: GlobalAuditClaim): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class GlobalAuditClaimAllowedCountryRule(var allowed: MutableList<String>) : GlobalAuditClaimRule {
    override fun check(item: GlobalAuditClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditClaimStartsAtRangeRule(var min: Int, var max: Int) : GlobalAuditClaimRule {
    override fun check(item: GlobalAuditClaim): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditClaimNotesLengthRule(var maxLength: Int) : GlobalAuditClaimRule {
    override fun check(item: GlobalAuditClaim): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditClaimRequiredNotesRule(var message: String) : GlobalAuditClaimRule {
    override fun check(item: GlobalAuditClaim): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global audit claim. */
data class GlobalAuditClaimValidator(var rules: MutableList<GlobalAuditClaimRule>) {
    fun validate(item: GlobalAuditClaim): MutableList<String> {
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

    fun requireValid(item: GlobalAuditClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAuditClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAuditClaimValidator(): GlobalAuditClaimValidator {
    return GlobalAuditClaimValidator(
        mutableListOf<GlobalAuditClaimRule>(
            GlobalAuditClaimEtaMinutesRangeRule(1000, 3600),
            GlobalAuditClaimAttemptsRangeRule(1000, 86400),
            GlobalAuditClaimAllowedCountryRule(mutableListOf<String>("manual", "rolling", "default")),
            GlobalAuditClaimStartsAtRangeRule(16, 64),
            GlobalAuditClaimNotesLengthRule(86400),
            GlobalAuditClaimRequiredNotesRule("global audit claim needs this field"),
        ),
    )
}
