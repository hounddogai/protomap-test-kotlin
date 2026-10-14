package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.text.normalize

/** A rule a zone audit must pass. Check returns its problems, or none. */
interface ZoneAuditRule {
    fun check(item: ZoneAudit): MutableList<String>
}

data class ZoneAuditRequiredLocaleRule(var message: String) : ZoneAuditRule {
    override fun check(item: ZoneAudit): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneAuditAllowedLocaleRule(var allowed: MutableList<String>) : ZoneAuditRule {
    override fun check(item: ZoneAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ZoneAuditCodePrefixRule(var prefix: String) : ZoneAuditRule {
    override fun check(item: ZoneAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneAuditRatioRangeRule(var min: Double, var max: Double) : ZoneAuditRule {
    override fun check(item: ZoneAudit): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone audit. */
data class ZoneAuditValidator(var rules: MutableList<ZoneAuditRule>) {
    fun validate(item: ZoneAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneAuditValidator(): ZoneAuditValidator {
    return ZoneAuditValidator(
        mutableListOf<ZoneAuditRule>(
            ZoneAuditRequiredLocaleRule("zone audit needs a value"),
            ZoneAuditAllowedLocaleRule(mutableListOf<String>("adaptive", "fallback")),
            ZoneAuditCodePrefixRule("ACM"),
            ZoneAuditRatioRangeRule(4.3, 649.3),
        ),
    )
}
