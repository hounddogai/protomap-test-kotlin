package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.text.normalize

/** A rule a capacity audit must pass. Check returns its problems, or none. */
interface CapacityAuditRule {
    fun check(item: CapacityAudit): MutableList<String>
}

data class CapacityAuditEtaMinutesRangeRule(var min: Int, var max: Int) : CapacityAuditRule {
    override fun check(item: CapacityAudit): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityAuditRevisionRangeRule(var min: Int, var max: Int) : CapacityAuditRule {
    override fun check(item: CapacityAudit): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityAuditChecksumLengthRule(var maxLength: Int) : CapacityAuditRule {
    override fun check(item: CapacityAudit): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityAuditAllowedCodeRule(var allowed: MutableList<String>) : CapacityAuditRule {
    override fun check(item: CapacityAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CapacityAuditRequiredChecksumRule(var message: String) : CapacityAuditRule {
    override fun check(item: CapacityAudit): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityAuditCodePrefixRule(var prefix: String) : CapacityAuditRule {
    override fun check(item: CapacityAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity audit. */
data class CapacityAuditValidator(var rules: MutableList<CapacityAuditRule>) {
    fun validate(item: CapacityAudit): MutableList<String> {
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

    fun requireValid(item: CapacityAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityAuditValidator(): CapacityAuditValidator {
    return CapacityAuditValidator(
        mutableListOf<CapacityAuditRule>(
            CapacityAuditEtaMinutesRangeRule(80, 120),
            CapacityAuditRevisionRangeRule(80, 3600),
            CapacityAuditChecksumLengthRule(32),
            CapacityAuditAllowedCodeRule(mutableListOf<String>("regional", "adaptive", "scheduled")),
            CapacityAuditRequiredChecksumRule("capacity audit needs this field"),
            CapacityAuditCodePrefixRule("REF"),
        ),
    )
}
