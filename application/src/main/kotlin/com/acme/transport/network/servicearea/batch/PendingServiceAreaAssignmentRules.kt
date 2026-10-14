package com.acme.transport.network.servicearea.batch

import com.acme.transport.kit.text.normalize

/** A rule a pending service area assignment must pass. Check returns its problems, or none. */
interface PendingServiceAreaAssignmentRule {
    fun check(item: PendingServiceAreaAssignment): MutableList<String>
}

data class PendingServiceAreaAssignmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : PendingServiceAreaAssignmentRule {
    override fun check(item: PendingServiceAreaAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaAssignmentSummaryLengthRule(
    var maxLength: Int,
) : PendingServiceAreaAssignmentRule {
    override fun check(item: PendingServiceAreaAssignment): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaAssignmentRequiredPhoneRule(
    var message: String,
) : PendingServiceAreaAssignmentRule {
    override fun check(item: PendingServiceAreaAssignment): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaAssignmentTitleLengthRule(var maxLength: Int) : PendingServiceAreaAssignmentRule {
    override fun check(item: PendingServiceAreaAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaAssignmentRequiredCodeRule(
    var message: String,
) : PendingServiceAreaAssignmentRule {
    override fun check(item: PendingServiceAreaAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaAssignmentRequiredSummaryRule(
    var message: String,
) : PendingServiceAreaAssignmentRule {
    override fun check(item: PendingServiceAreaAssignment): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending service area assignment. */
data class PendingServiceAreaAssignmentValidator(var rules: MutableList<PendingServiceAreaAssignmentRule>) {
    fun validate(item: PendingServiceAreaAssignment): MutableList<String> {
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

    fun requireValid(item: PendingServiceAreaAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingServiceAreaAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingServiceAreaAssignmentValidator(): PendingServiceAreaAssignmentValidator {
    return PendingServiceAreaAssignmentValidator(
        mutableListOf<PendingServiceAreaAssignmentRule>(
            PendingServiceAreaAssignmentAllowedTitleRule(mutableListOf<String>("global", "regional")),
            PendingServiceAreaAssignmentSummaryLengthRule(120),
            PendingServiceAreaAssignmentRequiredPhoneRule("pending service area assignment needs an entry"),
            PendingServiceAreaAssignmentTitleLengthRule(86400),
            PendingServiceAreaAssignmentRequiredCodeRule("pending service area assignment needs this field"),
            PendingServiceAreaAssignmentRequiredSummaryRule("pending service area assignment needs an entry"),
        ),
    )
}
