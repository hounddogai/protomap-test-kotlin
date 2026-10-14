package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.normalize

/** A rule a inspection checkpoint must pass. Check returns its problems, or none. */
interface InspectionCheckpointRule {
    fun check(item: InspectionCheckpoint): MutableList<String>
}

data class InspectionCheckpointRequiredCodeRule(var message: String) : InspectionCheckpointRule {
    override fun check(item: InspectionCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionCheckpointSummaryLengthRule(var maxLength: Int) : InspectionCheckpointRule {
    override fun check(item: InspectionCheckpoint): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionCheckpointAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : InspectionCheckpointRule {
    override fun check(item: InspectionCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection checkpoint. */
data class InspectionCheckpointValidator(var rules: MutableList<InspectionCheckpointRule>) {
    fun validate(item: InspectionCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionCheckpointValidator(): InspectionCheckpointValidator {
    return InspectionCheckpointValidator(
        mutableListOf<InspectionCheckpointRule>(
            InspectionCheckpointRequiredCodeRule("inspection checkpoint needs an entry"),
            InspectionCheckpointSummaryLengthRule(16),
            InspectionCheckpointAllowedContactEmailRule(mutableListOf<String>("scheduled", "tiered", "lenient")),
        ),
    )
}
