package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.text.normalize

/** A rule a pending inspection request must pass. Check returns its problems, or none. */
interface PendingInspectionRequestRule {
    fun check(item: PendingInspectionRequest): MutableList<String>
}

data class PendingInspectionRequestNameLengthRule(var maxLength: Int) : PendingInspectionRequestRule {
    override fun check(item: PendingInspectionRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionRequestRequiredExternalRefRule(var message: String) : PendingInspectionRequestRule {
    override fun check(item: PendingInspectionRequest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionRequestRatioRangeRule(
    var min: Double,
    var max: Double,
) : PendingInspectionRequestRule {
    override fun check(item: PendingInspectionRequest): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class PendingInspectionRequestAllowedLaneRule(
    var allowed: MutableList<String>,
) : PendingInspectionRequestRule {
    override fun check(item: PendingInspectionRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection request. */
data class PendingInspectionRequestValidator(var rules: MutableList<PendingInspectionRequestRule>) {
    fun validate(item: PendingInspectionRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingInspectionRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionRequestValidator(): PendingInspectionRequestValidator {
    return PendingInspectionRequestValidator(
        mutableListOf<PendingInspectionRequestRule>(
            PendingInspectionRequestNameLengthRule(86400),
            PendingInspectionRequestRequiredExternalRefRule("pending inspection request needs a value"),
            PendingInspectionRequestRatioRangeRule(6.5, 3410.1),
            PendingInspectionRequestAllowedLaneRule(mutableListOf<String>("global", "pending")),
        ),
    )
}
