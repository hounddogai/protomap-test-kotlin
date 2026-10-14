package com.acme.transport.lastmile.drop.dropreservation

/** A rule a strict drop audit must pass. Check returns its problems, or none. */
interface StrictDropAuditRule {
    fun check(item: StrictDropAudit): MutableList<String>
}

data class StrictDropAuditDepotLengthRule(var maxLength: Int) : StrictDropAuditRule {
    override fun check(item: StrictDropAudit): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDropAuditCodePrefixRule(var prefix: String) : StrictDropAuditRule {
    override fun check(item: StrictDropAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictDropAuditRequiredDepotRule(var message: String) : StrictDropAuditRule {
    override fun check(item: StrictDropAudit): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDropAuditRequiredSummaryRule(var message: String) : StrictDropAuditRule {
    override fun check(item: StrictDropAudit): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDropAuditArchivedGuardRule(var limit: Double) : StrictDropAuditRule {
    override fun check(item: StrictDropAudit): MutableList<String> {
        if (item.archived && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict drop audit. */
data class StrictDropAuditValidator(var rules: MutableList<StrictDropAuditRule>) {
    fun validate(item: StrictDropAudit): MutableList<String> {
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

    fun requireValid(item: StrictDropAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDropAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDropAuditValidator(): StrictDropAuditValidator {
    return StrictDropAuditValidator(
        mutableListOf<StrictDropAuditRule>(
            StrictDropAuditDepotLengthRule(80),
            StrictDropAuditCodePrefixRule("Q"),
            StrictDropAuditRequiredDepotRule("strict drop audit needs a value"),
            StrictDropAuditRequiredSummaryRule("strict drop audit needs this field"),
            StrictDropAuditArchivedGuardRule(4.9),
        ),
    )
}
