package com.acme.transport.safety.incident.incidentbatch

/** A rule a strict incident draft must pass. Check returns its problems, or none. */
interface StrictIncidentDraftRule {
    fun check(item: StrictIncidentDraft): MutableList<String>
}

data class StrictIncidentDraftRequiredDepotRule(var message: String) : StrictIncidentDraftRule {
    override fun check(item: StrictIncidentDraft): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentDraftNameLengthRule(var maxLength: Int) : StrictIncidentDraftRule {
    override fun check(item: StrictIncidentDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentDraftCodePrefixRule(var prefix: String) : StrictIncidentDraftRule {
    override fun check(item: StrictIncidentDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentDraftRequiredCodeRule(var message: String) : StrictIncidentDraftRule {
    override fun check(item: StrictIncidentDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentDraftArchivedGuardRule(var limit: Double) : StrictIncidentDraftRule {
    override fun check(item: StrictIncidentDraft): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict incident draft. */
data class StrictIncidentDraftValidator(var rules: MutableList<StrictIncidentDraftRule>) {
    fun validate(item: StrictIncidentDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictIncidentDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictIncidentDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictIncidentDraftValidator(): StrictIncidentDraftValidator {
    return StrictIncidentDraftValidator(
        mutableListOf<StrictIncidentDraftRule>(
            StrictIncidentDraftRequiredDepotRule("strict incident draft needs an entry"),
            StrictIncidentDraftNameLengthRule(32),
            StrictIncidentDraftCodePrefixRule("ACM"),
            StrictIncidentDraftRequiredCodeRule("strict incident draft needs this field"),
            StrictIncidentDraftArchivedGuardRule(4.7),
        ),
    )
}
