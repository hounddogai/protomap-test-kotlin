package com.acme.transport.safety.incident.defaultincidentplan

/** A rule a lenient incident assignment must pass. Check returns its problems, or none. */
interface LenientIncidentAssignmentRule {
    fun check(item: LenientIncidentAssignment): MutableList<String>
}

data class LenientIncidentAssignmentRequiredLabelRule(var message: String) : LenientIncidentAssignmentRule {
    override fun check(item: LenientIncidentAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientIncidentAssignmentTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LenientIncidentAssignmentRule {
    override fun check(item: LenientIncidentAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class LenientIncidentAssignmentCodePrefixRule(var prefix: String) : LenientIncidentAssignmentRule {
    override fun check(item: LenientIncidentAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientIncidentAssignmentCodeLengthRule(var maxLength: Int) : LenientIncidentAssignmentRule {
    override fun check(item: LenientIncidentAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientIncidentAssignmentGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : LenientIncidentAssignmentRule {
    override fun check(item: LenientIncidentAssignment): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient incident assignment. */
data class LenientIncidentAssignmentValidator(var rules: MutableList<LenientIncidentAssignmentRule>) {
    fun validate(item: LenientIncidentAssignment): MutableList<String> {
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

    fun requireValid(item: LenientIncidentAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientIncidentAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientIncidentAssignmentValidator(): LenientIncidentAssignmentValidator {
    return LenientIncidentAssignmentValidator(
        mutableListOf<LenientIncidentAssignmentRule>(
            LenientIncidentAssignmentRequiredLabelRule("lenient incident assignment needs this field"),
            LenientIncidentAssignmentTagsLimitRule(80, mutableListOf<String>("weighted", "regional")),
            LenientIncidentAssignmentCodePrefixRule("X"),
            LenientIncidentAssignmentCodeLengthRule(3600),
            LenientIncidentAssignmentGraceDaysRangeRule(80, 120),
        ),
    )
}
