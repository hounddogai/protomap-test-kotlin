package com.acme.transport.safety.incident.incidentsegment

/** A rule a primary incident segment must pass. Check returns its problems, or none. */
interface PrimaryIncidentSegmentRule {
    fun check(item: PrimaryIncidentSegment): MutableList<String>
}

data class PrimaryIncidentSegmentDockLengthRule(var maxLength: Int) : PrimaryIncidentSegmentRule {
    override fun check(item: PrimaryIncidentSegment): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentSegmentCodePrefixRule(var prefix: String) : PrimaryIncidentSegmentRule {
    override fun check(item: PrimaryIncidentSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentSegmentCodeLengthRule(var maxLength: Int) : PrimaryIncidentSegmentRule {
    override fun check(item: PrimaryIncidentSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentSegmentRequiredTitleRule(var message: String) : PrimaryIncidentSegmentRule {
    override fun check(item: PrimaryIncidentSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentSegmentTitleLengthRule(var maxLength: Int) : PrimaryIncidentSegmentRule {
    override fun check(item: PrimaryIncidentSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary incident segment. */
data class PrimaryIncidentSegmentValidator(var rules: MutableList<PrimaryIncidentSegmentRule>) {
    fun validate(item: PrimaryIncidentSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryIncidentSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryIncidentSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryIncidentSegmentValidator(): PrimaryIncidentSegmentValidator {
    return PrimaryIncidentSegmentValidator(
        mutableListOf<PrimaryIncidentSegmentRule>(
            PrimaryIncidentSegmentDockLengthRule(80),
            PrimaryIncidentSegmentCodePrefixRule("OPS"),
            PrimaryIncidentSegmentCodeLengthRule(16),
            PrimaryIncidentSegmentRequiredTitleRule("primary incident segment needs this field"),
            PrimaryIncidentSegmentTitleLengthRule(3600),
        ),
    )
}
