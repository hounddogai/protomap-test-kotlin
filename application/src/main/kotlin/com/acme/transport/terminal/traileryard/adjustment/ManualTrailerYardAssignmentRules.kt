package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a manual trailer yard assignment must pass. Check returns its problems, or none. */
interface ManualTrailerYardAssignmentRule {
    fun check(item: ManualTrailerYardAssignment): MutableList<String>
}

data class ManualTrailerYardAssignmentCodeLengthRule(var maxLength: Int) : ManualTrailerYardAssignmentRule {
    override fun check(item: ManualTrailerYardAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardAssignmentAllowedReviewerRule(
    var allowed: MutableList<String>,
) : ManualTrailerYardAssignmentRule {
    override fun check(item: ManualTrailerYardAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardAssignmentTitleLengthRule(var maxLength: Int) : ManualTrailerYardAssignmentRule {
    override fun check(item: ManualTrailerYardAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardAssignmentRequiredReviewerRule(
    var message: String,
) : ManualTrailerYardAssignmentRule {
    override fun check(item: ManualTrailerYardAssignment): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer yard assignment. */
data class ManualTrailerYardAssignmentValidator(var rules: MutableList<ManualTrailerYardAssignmentRule>) {
    fun validate(item: ManualTrailerYardAssignment): MutableList<String> {
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

    fun requireValid(item: ManualTrailerYardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerYardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerYardAssignmentValidator(): ManualTrailerYardAssignmentValidator {
    return ManualTrailerYardAssignmentValidator(
        mutableListOf<ManualTrailerYardAssignmentRule>(
            ManualTrailerYardAssignmentCodeLengthRule(255),
            ManualTrailerYardAssignmentAllowedReviewerRule(mutableListOf<String>()),
            ManualTrailerYardAssignmentTitleLengthRule(3600),
            ManualTrailerYardAssignmentRequiredReviewerRule("manual trailer yard assignment needs a value"),
        ),
    )
}
