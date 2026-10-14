package com.acme.transport.linehaul.laneplan.preference

/** A rule a legacy lane plan target must pass. Check returns its problems, or none. */
interface LegacyLanePlanTargetRule {
    fun check(item: LegacyLanePlanTarget): MutableList<String>
}

data class LegacyLanePlanTargetTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LegacyLanePlanTargetRule {
    override fun check(item: LegacyLanePlanTarget): MutableList<String> {
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

data class LegacyLanePlanTargetSegmentLengthRule(var maxLength: Int) : LegacyLanePlanTargetRule {
    override fun check(item: LegacyLanePlanTarget): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanTargetCodePrefixRule(var prefix: String) : LegacyLanePlanTargetRule {
    override fun check(item: LegacyLanePlanTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanTargetRequiredLabelRule(var message: String) : LegacyLanePlanTargetRule {
    override fun check(item: LegacyLanePlanTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanTargetRequiredSegmentRule(var message: String) : LegacyLanePlanTargetRule {
    override fun check(item: LegacyLanePlanTarget): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lane plan target. */
data class LegacyLanePlanTargetValidator(var rules: MutableList<LegacyLanePlanTargetRule>) {
    fun validate(item: LegacyLanePlanTarget): MutableList<String> {
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

    fun requireValid(item: LegacyLanePlanTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLanePlanTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLanePlanTargetValidator(): LegacyLanePlanTargetValidator {
    return LegacyLanePlanTargetValidator(
        mutableListOf<LegacyLanePlanTargetRule>(
            LegacyLanePlanTargetTagsLimitRule(16, mutableListOf<String>("adaptive")),
            LegacyLanePlanTargetSegmentLengthRule(32),
            LegacyLanePlanTargetCodePrefixRule("ACM"),
            LegacyLanePlanTargetRequiredLabelRule("legacy lane plan target needs a value"),
            LegacyLanePlanTargetRequiredSegmentRule("legacy lane plan target needs an entry"),
        ),
    )
}
