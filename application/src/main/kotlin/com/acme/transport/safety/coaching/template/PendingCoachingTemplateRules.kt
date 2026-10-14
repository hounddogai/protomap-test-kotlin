package com.acme.transport.safety.coaching.template

/** A rule a pending coaching template must pass. Check returns its problems, or none. */
interface PendingCoachingTemplateRule {
    fun check(item: PendingCoachingTemplate): MutableList<String>
}

data class PendingCoachingTemplateCodePrefixRule(var prefix: String) : PendingCoachingTemplateRule {
    override fun check(item: PendingCoachingTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingTemplateFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PendingCoachingTemplateRule {
    override fun check(item: PendingCoachingTemplate): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class PendingCoachingTemplatePinnedGuardRule(var limit: Double) : PendingCoachingTemplateRule {
    override fun check(item: PendingCoachingTemplate): MutableList<String> {
        if (item.pinned && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingTemplateRegionLengthRule(var maxLength: Int) : PendingCoachingTemplateRule {
    override fun check(item: PendingCoachingTemplate): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingTemplateRequiredRegionRule(var message: String) : PendingCoachingTemplateRule {
    override fun check(item: PendingCoachingTemplate): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending coaching template. */
data class PendingCoachingTemplateValidator(var rules: MutableList<PendingCoachingTemplateRule>) {
    fun validate(item: PendingCoachingTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCoachingTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCoachingTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCoachingTemplateValidator(): PendingCoachingTemplateValidator {
    return PendingCoachingTemplateValidator(
        mutableListOf<PendingCoachingTemplateRule>(
            PendingCoachingTemplateCodePrefixRule("X"),
            PendingCoachingTemplateFloorValueRangeRule(4.3, 1338.2),
            PendingCoachingTemplatePinnedGuardRule(0.7),
            PendingCoachingTemplateRegionLengthRule(16),
            PendingCoachingTemplateRequiredRegionRule("pending coaching template needs a value"),
        ),
    )
}
