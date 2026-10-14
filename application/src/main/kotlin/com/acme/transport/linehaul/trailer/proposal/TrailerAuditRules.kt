package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.text.normalize

/** A rule a trailer audit must pass. Check returns its problems, or none. */
interface TrailerAuditRule {
    fun check(item: TrailerAudit): MutableList<String>
}

data class TrailerAuditRequiredNameRule(var message: String) : TrailerAuditRule {
    override fun check(item: TrailerAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerAuditAllowedDepotRule(var allowed: MutableList<String>) : TrailerAuditRule {
    override fun check(item: TrailerAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TrailerAuditCodePrefixRule(var prefix: String) : TrailerAuditRule {
    override fun check(item: TrailerAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerAuditRequiredCodeRule(var message: String) : TrailerAuditRule {
    override fun check(item: TrailerAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerAuditCreatedAtRangeRule(var min: Int, var max: Int) : TrailerAuditRule {
    override fun check(item: TrailerAudit): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a trailer audit. */
data class TrailerAuditValidator(var rules: MutableList<TrailerAuditRule>) {
    fun validate(item: TrailerAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerAuditValidator(): TrailerAuditValidator {
    return TrailerAuditValidator(
        mutableListOf<TrailerAuditRule>(
            TrailerAuditRequiredNameRule("trailer audit needs a value"),
            TrailerAuditAllowedDepotRule(mutableListOf<String>("strict", "strict", "adaptive")),
            TrailerAuditCodePrefixRule("X"),
            TrailerAuditRequiredCodeRule("trailer audit needs this field"),
            TrailerAuditCreatedAtRangeRule(64, 86400),
        ),
    )
}
