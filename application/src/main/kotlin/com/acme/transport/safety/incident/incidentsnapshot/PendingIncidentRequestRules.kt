package com.acme.transport.safety.incident.incidentsnapshot

/** A rule a pending incident request must pass. Check returns its problems, or none. */
interface PendingIncidentRequestRule {
    fun check(item: PendingIncidentRequest): MutableList<String>
}

data class PendingIncidentRequestRequiredTitleRule(var message: String) : PendingIncidentRequestRule {
    override fun check(item: PendingIncidentRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingIncidentRequestTitleLengthRule(var maxLength: Int) : PendingIncidentRequestRule {
    override fun check(item: PendingIncidentRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingIncidentRequestTimezoneLengthRule(var maxLength: Int) : PendingIncidentRequestRule {
    override fun check(item: PendingIncidentRequest): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending incident request. */
data class PendingIncidentRequestValidator(var rules: MutableList<PendingIncidentRequestRule>) {
    fun validate(item: PendingIncidentRequest): MutableList<String> {
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

    fun requireValid(item: PendingIncidentRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingIncidentRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingIncidentRequestValidator(): PendingIncidentRequestValidator {
    return PendingIncidentRequestValidator(
        mutableListOf<PendingIncidentRequestRule>(
            PendingIncidentRequestRequiredTitleRule("pending incident request needs an entry"),
            PendingIncidentRequestTitleLengthRule(3600),
            PendingIncidentRequestTimezoneLengthRule(1000),
        ),
    )
}
