package com.acme.transport.customs.inspection.inspectionmilestone

/** A rule a inspection threshold must pass. Check returns its problems, or none. */
interface InspectionThresholdRule {
    fun check(item: InspectionThreshold): MutableList<String>
}

data class InspectionThresholdTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : InspectionThresholdRule {
    override fun check(item: InspectionThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class InspectionThresholdActiveGuardRule(var limit: Double) : InspectionThresholdRule {
    override fun check(item: InspectionThreshold): MutableList<String> {
        if (item.active && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class InspectionThresholdVersionRangeRule(var min: Int, var max: Int) : InspectionThresholdRule {
    override fun check(item: InspectionThreshold): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionThresholdCodePrefixRule(var prefix: String) : InspectionThresholdRule {
    override fun check(item: InspectionThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionThresholdRequiredOwnerIdRule(var message: String) : InspectionThresholdRule {
    override fun check(item: InspectionThreshold): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionThresholdSequenceRangeRule(var min: Int, var max: Int) : InspectionThresholdRule {
    override fun check(item: InspectionThreshold): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
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
            InspectionThresholdTagsLimitRule(3600, mutableListOf<String>("batched", "capped")),
            InspectionThresholdActiveGuardRule(6.8),
            InspectionThresholdVersionRangeRule(16, 3600),
            InspectionThresholdCodePrefixRule("X"),
            InspectionThresholdRequiredOwnerIdRule("inspection threshold needs an entry"),
            InspectionThresholdSequenceRangeRule(16, 255),
        ),
    )
}
