package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.text.normalize

/** A rule a inspection threshold must pass. Check returns its problems, or none. */
interface InspectionThresholdRule {
    fun check(item: InspectionThreshold): MutableList<String>
}

data class InspectionThresholdAllowedSourceRule(var allowed: MutableList<String>) : InspectionThresholdRule {
    override fun check(item: InspectionThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is too long")
        }
        return mutableListOf<String>()
    }
}

data class InspectionThresholdRequiredDriverIdRule(var message: String) : InspectionThresholdRule {
    override fun check(item: InspectionThreshold): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionThresholdRequiredCodeRule(var message: String) : InspectionThresholdRule {
    override fun check(item: InspectionThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection threshold. */
data class InspectionThresholdValidator(var rules: MutableList<InspectionThresholdRule>) {
    fun validate(item: InspectionThreshold): MutableList<String> {
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

    fun requireValid(item: InspectionThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionThresholdValidator(): InspectionThresholdValidator {
    return InspectionThresholdValidator(
        mutableListOf<InspectionThresholdRule>(
            InspectionThresholdAllowedSourceRule(mutableListOf<String>("regional", "global", "adaptive")),
            InspectionThresholdRequiredDriverIdRule("inspection threshold needs an entry"),
            InspectionThresholdRequiredCodeRule("inspection threshold needs a value"),
        ),
    )
}
