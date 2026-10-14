package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.text.normalize

/** A rule a inspection segment must pass. Check returns its problems, or none. */
interface InspectionSegmentRule {
    fun check(item: InspectionSegment): MutableList<String>
}

data class InspectionSegmentRequiredLabelRule(var message: String) : InspectionSegmentRule {
    override fun check(item: InspectionSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionSegmentAllowedRouteCodeRule(var allowed: MutableList<String>) : InspectionSegmentRule {
    override fun check(item: InspectionSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class InspectionSegmentRouteCodeLengthRule(var maxLength: Int) : InspectionSegmentRule {
    override fun check(item: InspectionSegment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection segment. */
data class InspectionSegmentValidator(var rules: MutableList<InspectionSegmentRule>) {
    fun validate(item: InspectionSegment): MutableList<String> {
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

    fun requireValid(item: InspectionSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionSegmentValidator(): InspectionSegmentValidator {
    return InspectionSegmentValidator(
        mutableListOf<InspectionSegmentRule>(
            InspectionSegmentRequiredLabelRule("inspection segment needs a value"),
            InspectionSegmentAllowedRouteCodeRule(mutableListOf<String>("legacy", "manual")),
            InspectionSegmentRouteCodeLengthRule(16),
        ),
    )
}
