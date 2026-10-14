package com.acme.transport.terminal.traileryard.audit

/** A rule a global trailer yard audit must pass. Check returns its problems, or none. */
interface GlobalTrailerYardAuditRule {
    fun check(item: GlobalTrailerYardAudit): MutableList<String>
}

data class GlobalTrailerYardAuditNameLengthRule(var maxLength: Int) : GlobalTrailerYardAuditRule {
    override fun check(item: GlobalTrailerYardAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardAuditArchivedGuardRule(var limit: Double) : GlobalTrailerYardAuditRule {
    override fun check(item: GlobalTrailerYardAudit): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardAuditRequiredNameRule(var message: String) : GlobalTrailerYardAuditRule {
    override fun check(item: GlobalTrailerYardAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardAuditRequiredDescriptionRule(var message: String) : GlobalTrailerYardAuditRule {
    override fun check(item: GlobalTrailerYardAudit): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardAuditCodeLengthRule(var maxLength: Int) : GlobalTrailerYardAuditRule {
    override fun check(item: GlobalTrailerYardAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardAuditLaneLengthRule(var maxLength: Int) : GlobalTrailerYardAuditRule {
    override fun check(item: GlobalTrailerYardAudit): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global trailer yard audit. */
data class GlobalTrailerYardAuditValidator(var rules: MutableList<GlobalTrailerYardAuditRule>) {
    fun validate(item: GlobalTrailerYardAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTrailerYardAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerYardAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerYardAuditValidator(): GlobalTrailerYardAuditValidator {
    return GlobalTrailerYardAuditValidator(
        mutableListOf<GlobalTrailerYardAuditRule>(
            GlobalTrailerYardAuditNameLengthRule(16),
            GlobalTrailerYardAuditArchivedGuardRule(8.3),
            GlobalTrailerYardAuditRequiredNameRule("global trailer yard audit needs an entry"),
            GlobalTrailerYardAuditRequiredDescriptionRule("global trailer yard audit needs this field"),
            GlobalTrailerYardAuditCodeLengthRule(64),
            GlobalTrailerYardAuditLaneLengthRule(64),
        ),
    )
}
