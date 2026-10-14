package com.acme.transport.fleetops.utilization.audit

/** A rule a weighted utilization audit must pass. Check returns its problems, or none. */
interface WeightedUtilizationAuditRule {
    fun check(item: WeightedUtilizationAudit): MutableList<String>
}

data class WeightedUtilizationAuditCodeLengthRule(var maxLength: Int) : WeightedUtilizationAuditRule {
    override fun check(item: WeightedUtilizationAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationAuditPriorityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedUtilizationAuditRule {
    override fun check(item: WeightedUtilizationAudit): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class WeightedUtilizationAuditTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : WeightedUtilizationAuditRule {
    override fun check(item: WeightedUtilizationAudit): MutableList<String> {
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

data class WeightedUtilizationAuditStopCountRangeRule(
    var min: Int,
    var max: Int,
) : WeightedUtilizationAuditRule {
    override fun check(item: WeightedUtilizationAudit): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class WeightedUtilizationAuditRequiredTitleRule(var message: String) : WeightedUtilizationAuditRule {
    override fun check(item: WeightedUtilizationAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationAuditSummaryLengthRule(var maxLength: Int) : WeightedUtilizationAuditRule {
    override fun check(item: WeightedUtilizationAudit): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted utilization audit. */
data class WeightedUtilizationAuditValidator(var rules: MutableList<WeightedUtilizationAuditRule>) {
    fun validate(item: WeightedUtilizationAudit): MutableList<String> {
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

    fun requireValid(item: WeightedUtilizationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedUtilizationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedUtilizationAuditValidator(): WeightedUtilizationAuditValidator {
    return WeightedUtilizationAuditValidator(
        mutableListOf<WeightedUtilizationAuditRule>(
            WeightedUtilizationAuditCodeLengthRule(255),
            WeightedUtilizationAuditPriorityRangeRule(32, 1000),
            WeightedUtilizationAuditTagsLimitRule(86400, mutableListOf<String>("batched", "rolling")),
            WeightedUtilizationAuditStopCountRangeRule(255, 3600),
            WeightedUtilizationAuditRequiredTitleRule("weighted utilization audit needs an entry"),
            WeightedUtilizationAuditSummaryLengthRule(64),
        ),
    )
}
