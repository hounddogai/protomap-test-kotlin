package com.acme.transport.crewing.roster.quota

/** A rule a scheduled roster profile must pass. Check returns its problems, or none. */
interface ScheduledRosterProfileRule {
    fun check(item: ScheduledRosterProfile): MutableList<String>
}

data class ScheduledRosterProfileArchivedGuardRule(var limit: Int) : ScheduledRosterProfileRule {
    override fun check(item: ScheduledRosterProfile): MutableList<String> {
        if (item.archived && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterProfileCodePrefixRule(var prefix: String) : ScheduledRosterProfileRule {
    override fun check(item: ScheduledRosterProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterProfileCodeLengthRule(var maxLength: Int) : ScheduledRosterProfileRule {
    override fun check(item: ScheduledRosterProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterProfileRequiredShiftCodeRule(var message: String) : ScheduledRosterProfileRule {
    override fun check(item: ScheduledRosterProfile): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled roster profile. */
data class ScheduledRosterProfileValidator(var rules: MutableList<ScheduledRosterProfileRule>) {
    fun validate(item: ScheduledRosterProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRosterProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRosterProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRosterProfileValidator(): ScheduledRosterProfileValidator {
    return ScheduledRosterProfileValidator(
        mutableListOf<ScheduledRosterProfileRule>(
            ScheduledRosterProfileArchivedGuardRule(255),
            ScheduledRosterProfileCodePrefixRule("REF"),
            ScheduledRosterProfileCodeLengthRule(64),
            ScheduledRosterProfileRequiredShiftCodeRule("scheduled roster profile needs an entry"),
        ),
    )
}
