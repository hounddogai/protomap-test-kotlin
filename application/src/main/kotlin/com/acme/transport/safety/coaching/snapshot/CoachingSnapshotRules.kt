package com.acme.transport.safety.coaching.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a coaching snapshot must pass. Check returns its problems, or none. */
interface CoachingSnapshotRule {
    fun check(item: CoachingSnapshot): MutableList<String>
}

data class CoachingSnapshotCodeLengthRule(var maxLength: Int) : CoachingSnapshotRule {
    override fun check(item: CoachingSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingSnapshotRequiredLabelRule(var message: String) : CoachingSnapshotRule {
    override fun check(item: CoachingSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingSnapshotRequiredNotesRule(var message: String) : CoachingSnapshotRule {
    override fun check(item: CoachingSnapshot): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingSnapshotAllowedSourceRule(var allowed: MutableList<String>) : CoachingSnapshotRule {
    override fun check(item: CoachingSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CoachingSnapshotLabelLengthRule(var maxLength: Int) : CoachingSnapshotRule {
    override fun check(item: CoachingSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching snapshot. */
data class CoachingSnapshotValidator(var rules: MutableList<CoachingSnapshotRule>) {
    fun validate(item: CoachingSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingSnapshotValidator(): CoachingSnapshotValidator {
    return CoachingSnapshotValidator(
        mutableListOf<CoachingSnapshotRule>(
            CoachingSnapshotCodeLengthRule(255),
            CoachingSnapshotRequiredLabelRule("coaching snapshot needs a value"),
            CoachingSnapshotRequiredNotesRule("coaching snapshot needs a value"),
            CoachingSnapshotAllowedSourceRule(mutableListOf<String>("manual")),
            CoachingSnapshotLabelLengthRule(64),
        ),
    )
}
