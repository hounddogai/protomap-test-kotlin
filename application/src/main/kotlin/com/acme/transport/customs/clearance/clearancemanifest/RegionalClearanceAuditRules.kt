package com.acme.transport.customs.clearance.clearancemanifest

/** A rule a regional clearance audit must pass. Check returns its problems, or none. */
interface RegionalClearanceAuditRule {
    fun check(item: RegionalClearanceAudit): MutableList<String>
}

data class RegionalClearanceAuditRequiredCodeRule(var message: String) : RegionalClearanceAuditRule {
    override fun check(item: RegionalClearanceAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceAuditArchivedGuardRule(var limit: Double) : RegionalClearanceAuditRule {
    override fun check(item: RegionalClearanceAudit): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceAuditThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RegionalClearanceAuditRule {
    override fun check(item: RegionalClearanceAudit): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceAuditStartsAtRangeRule(var min: Int, var max: Int) : RegionalClearanceAuditRule {
    override fun check(item: RegionalClearanceAudit): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class RegionalClearanceAuditNameLengthRule(var maxLength: Int) : RegionalClearanceAuditRule {
    override fun check(item: RegionalClearanceAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceAuditSourceLengthRule(var maxLength: Int) : RegionalClearanceAuditRule {
    override fun check(item: RegionalClearanceAudit): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional clearance audit. */
data class RegionalClearanceAuditValidator(var rules: MutableList<RegionalClearanceAuditRule>) {
    fun validate(item: RegionalClearanceAudit): MutableList<String> {
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

    fun requireValid(item: RegionalClearanceAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalClearanceAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalClearanceAuditValidator(): RegionalClearanceAuditValidator {
    return RegionalClearanceAuditValidator(
        mutableListOf<RegionalClearanceAuditRule>(
            RegionalClearanceAuditRequiredCodeRule("regional clearance audit needs a value"),
            RegionalClearanceAuditArchivedGuardRule(6.5),
            RegionalClearanceAuditThresholdRangeRule(3.7, 3309.7),
            RegionalClearanceAuditStartsAtRangeRule(32, 86400),
            RegionalClearanceAuditNameLengthRule(120),
            RegionalClearanceAuditSourceLengthRule(255),
        ),
    )
}
