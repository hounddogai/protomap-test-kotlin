package com.acme.transport.fleetops.inspection.adjustment

/** A rule a inspection audit must pass. Check returns its problems, or none. */
interface InspectionAuditRule {
    fun check(item: InspectionAudit): MutableList<String>
}

data class InspectionAuditPinnedGuardRule(var limit: Double) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAuditTitleLengthRule(var maxLength: Int) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAuditOwnerIdLengthRule(var maxLength: Int) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAuditDurationMinutesRangeRule(var min: Int, var max: Int) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class InspectionAuditTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : InspectionAuditRule {
    override fun check(item: InspectionAudit): MutableList<String> {
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

/** Runs every rule of a inspection audit. */
data class InspectionAuditValidator(var rules: MutableList<InspectionAuditRule>) {
    fun validate(item: InspectionAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionAuditValidator(): InspectionAuditValidator {
    return InspectionAuditValidator(
        mutableListOf<InspectionAuditRule>(
            InspectionAuditPinnedGuardRule(3.7),
            InspectionAuditTitleLengthRule(64),
            InspectionAuditOwnerIdLengthRule(16),
            InspectionAuditDurationMinutesRangeRule(80, 3600),
            InspectionAuditTagsLimitRule(64, mutableListOf<String>("lenient")),
        ),
    )
}
