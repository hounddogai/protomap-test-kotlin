package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.normalize

/** A rule a lenient cutoff assignment must pass. Check returns its problems, or none. */
interface LenientCutoffAssignmentRule {
    fun check(item: LenientCutoffAssignment): MutableList<String>
}

data class LenientCutoffAssignmentUpdatedAtRangeRule(var min: Int, var max: Int) : LenientCutoffAssignmentRule {
    override fun check(item: LenientCutoffAssignment): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffAssignmentCodePrefixRule(var prefix: String) : LenientCutoffAssignmentRule {
    override fun check(item: LenientCutoffAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffAssignmentRequiredNameRule(var message: String) : LenientCutoffAssignmentRule {
    override fun check(item: LenientCutoffAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffAssignmentAllowedNotesRule(
    var allowed: MutableList<String>,
) : LenientCutoffAssignmentRule {
    override fun check(item: LenientCutoffAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient cutoff assignment. */
data class LenientCutoffAssignmentValidator(var rules: MutableList<LenientCutoffAssignmentRule>) {
    fun validate(item: LenientCutoffAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientCutoffAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCutoffAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCutoffAssignmentValidator(): LenientCutoffAssignmentValidator {
    return LenientCutoffAssignmentValidator(
        mutableListOf<LenientCutoffAssignmentRule>(
            LenientCutoffAssignmentUpdatedAtRangeRule(16, 64),
            LenientCutoffAssignmentCodePrefixRule("Q"),
            LenientCutoffAssignmentRequiredNameRule("lenient cutoff assignment needs this field"),
            LenientCutoffAssignmentAllowedNotesRule(mutableListOf<String>("regional", "fallback")),
        ),
    )
}
