package com.acme.transport.safety.scorecard.assignment

/** A rule a pending scorecard assignment must pass. Check returns its problems, or none. */
interface PendingScorecardAssignmentRule {
    fun check(item: PendingScorecardAssignment): MutableList<String>
}

data class PendingScorecardAssignmentContactEmailLengthRule(
    var maxLength: Int,
) : PendingScorecardAssignmentRule {
    override fun check(item: PendingScorecardAssignment): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardAssignmentRequiredTitleRule(var message: String) : PendingScorecardAssignmentRule {
    override fun check(item: PendingScorecardAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardAssignmentRequiredDockRule(var message: String) : PendingScorecardAssignmentRule {
    override fun check(item: PendingScorecardAssignment): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardAssignmentRequiredCodeRule(var message: String) : PendingScorecardAssignmentRule {
    override fun check(item: PendingScorecardAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardAssignmentCodePrefixRule(var prefix: String) : PendingScorecardAssignmentRule {
    override fun check(item: PendingScorecardAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardAssignmentRequiredContactEmailRule(
    var message: String,
) : PendingScorecardAssignmentRule {
    override fun check(item: PendingScorecardAssignment): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending scorecard assignment. */
data class PendingScorecardAssignmentValidator(var rules: MutableList<PendingScorecardAssignmentRule>) {
    fun validate(item: PendingScorecardAssignment): MutableList<String> {
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

    fun requireValid(item: PendingScorecardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingScorecardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingScorecardAssignmentValidator(): PendingScorecardAssignmentValidator {
    return PendingScorecardAssignmentValidator(
        mutableListOf<PendingScorecardAssignmentRule>(
            PendingScorecardAssignmentContactEmailLengthRule(3600),
            PendingScorecardAssignmentRequiredTitleRule("pending scorecard assignment needs a value"),
            PendingScorecardAssignmentRequiredDockRule("pending scorecard assignment needs a value"),
            PendingScorecardAssignmentRequiredCodeRule("pending scorecard assignment needs an entry"),
            PendingScorecardAssignmentCodePrefixRule("Q"),
            PendingScorecardAssignmentRequiredContactEmailRule("pending scorecard assignment needs a value"),
        ),
    )
}
