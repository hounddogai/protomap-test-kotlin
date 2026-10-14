package com.acme.transport.safety.scorecard.proposal

/** A rule a scorecard audit must pass. Check returns its problems, or none. */
interface ScorecardAuditRule {
    fun check(item: ScorecardAudit): MutableList<String>
}

data class ScorecardAuditArchivedGuardRule(var limit: Int) : ScorecardAuditRule {
    override fun check(item: ScorecardAudit): MutableList<String> {
        if (item.archived && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAuditRequiredCodeRule(var message: String) : ScorecardAuditRule {
    override fun check(item: ScorecardAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAuditCodePrefixRule(var prefix: String) : ScorecardAuditRule {
    override fun check(item: ScorecardAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard audit. */
data class ScorecardAuditValidator(var rules: MutableList<ScorecardAuditRule>) {
    fun validate(item: ScorecardAudit): MutableList<String> {
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

    fun requireValid(item: ScorecardAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardAuditValidator(): ScorecardAuditValidator {
    return ScorecardAuditValidator(
        mutableListOf<ScorecardAuditRule>(
            ScorecardAuditArchivedGuardRule(16),
            ScorecardAuditRequiredCodeRule("scorecard audit needs a value"),
            ScorecardAuditCodePrefixRule("OPS"),
        ),
    )
}
