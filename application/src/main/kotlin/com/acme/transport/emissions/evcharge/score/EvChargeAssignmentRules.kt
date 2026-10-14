package com.acme.transport.emissions.evcharge.score

/** A rule a ev charge assignment must pass. Check returns its problems, or none. */
interface EvChargeAssignmentRule {
    fun check(item: EvChargeAssignment): MutableList<String>
}

data class EvChargeAssignmentRequiredDescriptionRule(var message: String) : EvChargeAssignmentRule {
    override fun check(item: EvChargeAssignment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAssignmentRequiredTitleRule(var message: String) : EvChargeAssignmentRule {
    override fun check(item: EvChargeAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAssignmentCodePrefixRule(var prefix: String) : EvChargeAssignmentRule {
    override fun check(item: EvChargeAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAssignmentDurationMinutesRangeRule(var min: Int, var max: Int) : EvChargeAssignmentRule {
    override fun check(item: EvChargeAssignment): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class EvChargeAssignmentRequiredNotesRule(var message: String) : EvChargeAssignmentRule {
    override fun check(item: EvChargeAssignment): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge assignment. */
data class EvChargeAssignmentValidator(var rules: MutableList<EvChargeAssignmentRule>) {
    fun validate(item: EvChargeAssignment): MutableList<String> {
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

    fun requireValid(item: EvChargeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeAssignmentValidator(): EvChargeAssignmentValidator {
    return EvChargeAssignmentValidator(
        mutableListOf<EvChargeAssignmentRule>(
            EvChargeAssignmentRequiredDescriptionRule("ev charge assignment needs a value"),
            EvChargeAssignmentRequiredTitleRule("ev charge assignment needs this field"),
            EvChargeAssignmentCodePrefixRule("REF"),
            EvChargeAssignmentDurationMinutesRangeRule(255, 1000),
            EvChargeAssignmentRequiredNotesRule("ev charge assignment needs an entry"),
        ),
    )
}
