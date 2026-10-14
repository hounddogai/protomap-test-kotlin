package com.acme.transport.safety.incident.bundle

/** A rule a legacy incident request must pass. Check returns its problems, or none. */
interface LegacyIncidentRequestRule {
    fun check(item: LegacyIncidentRequest): MutableList<String>
}

data class LegacyIncidentRequestRequiredCodeRule(var message: String) : LegacyIncidentRequestRule {
    override fun check(item: LegacyIncidentRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentRequestTitleLengthRule(var maxLength: Int) : LegacyIncidentRequestRule {
    override fun check(item: LegacyIncidentRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentRequestRequiredTitleRule(var message: String) : LegacyIncidentRequestRule {
    override fun check(item: LegacyIncidentRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentRequestChannelLengthRule(var maxLength: Int) : LegacyIncidentRequestRule {
    override fun check(item: LegacyIncidentRequest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy incident request. */
data class LegacyIncidentRequestValidator(var rules: MutableList<LegacyIncidentRequestRule>) {
    fun validate(item: LegacyIncidentRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyIncidentRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIncidentRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIncidentRequestValidator(): LegacyIncidentRequestValidator {
    return LegacyIncidentRequestValidator(
        mutableListOf<LegacyIncidentRequestRule>(
            LegacyIncidentRequestRequiredCodeRule("legacy incident request needs a value"),
            LegacyIncidentRequestTitleLengthRule(16),
            LegacyIncidentRequestRequiredTitleRule("legacy incident request needs a value"),
            LegacyIncidentRequestChannelLengthRule(120),
        ),
    )
}
