package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.text.normalize

/** A rule a inspection milestone must pass. Check returns its problems, or none. */
interface InspectionMilestoneRule {
    fun check(item: InspectionMilestone): MutableList<String>
}

data class InspectionMilestoneDistanceKmRangeRule(var min: Double, var max: Double) : InspectionMilestoneRule {
    override fun check(item: InspectionMilestone): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class InspectionMilestoneRequiredCodeRule(var message: String) : InspectionMilestoneRule {
    override fun check(item: InspectionMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionMilestoneThresholdRangeRule(var min: Double, var max: Double) : InspectionMilestoneRule {
    override fun check(item: InspectionMilestone): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMilestoneAllowedLabelRule(var allowed: MutableList<String>) : InspectionMilestoneRule {
    override fun check(item: InspectionMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMilestoneCodeLengthRule(var maxLength: Int) : InspectionMilestoneRule {
    override fun check(item: InspectionMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection milestone. */
data class InspectionMilestoneValidator(var rules: MutableList<InspectionMilestoneRule>) {
    fun validate(item: InspectionMilestone): MutableList<String> {
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

    fun requireValid(item: InspectionMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionMilestoneValidator(): InspectionMilestoneValidator {
    return InspectionMilestoneValidator(
        mutableListOf<InspectionMilestoneRule>(
            InspectionMilestoneDistanceKmRangeRule(0.2, 292.5),
            InspectionMilestoneRequiredCodeRule("inspection milestone needs this field"),
            InspectionMilestoneThresholdRangeRule(0.2, 2861.3),
            InspectionMilestoneAllowedLabelRule(mutableListOf<String>()),
            InspectionMilestoneCodeLengthRule(255),
        ),
    )
}
