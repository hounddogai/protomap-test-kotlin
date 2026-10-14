package com.acme.transport.linehaul.laneplan.review

/** A rule a adaptive lane plan rule must pass. Check returns its problems, or none. */
interface AdaptiveLanePlanRuleRule {
    fun check(item: AdaptiveLanePlanRule): MutableList<String>
}

data class AdaptiveLanePlanRuleRequiredCodeRule(var message: String) : AdaptiveLanePlanRuleRule {
    override fun check(item: AdaptiveLanePlanRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanRuleCodePrefixRule(var prefix: String) : AdaptiveLanePlanRuleRule {
    override fun check(item: AdaptiveLanePlanRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanRuleArchivedGuardRule(var limit: Double) : AdaptiveLanePlanRuleRule {
    override fun check(item: AdaptiveLanePlanRule): MutableList<String> {
        if (item.archived && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanRuleSequenceRangeRule(var min: Int, var max: Int) : AdaptiveLanePlanRuleRule {
    override fun check(item: AdaptiveLanePlanRule): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class AdaptiveLanePlanRuleCategoryLengthRule(var maxLength: Int) : AdaptiveLanePlanRuleRule {
    override fun check(item: AdaptiveLanePlanRule): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lane plan rule. */
data class AdaptiveLanePlanRuleValidator(var rules: MutableList<AdaptiveLanePlanRuleRule>) {
    fun validate(item: AdaptiveLanePlanRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveLanePlanRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLanePlanRule>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLanePlanRuleValidator(): AdaptiveLanePlanRuleValidator {
    return AdaptiveLanePlanRuleValidator(
        mutableListOf<AdaptiveLanePlanRuleRule>(
            AdaptiveLanePlanRuleRequiredCodeRule("adaptive lane plan rule needs this field"),
            AdaptiveLanePlanRuleCodePrefixRule("X"),
            AdaptiveLanePlanRuleArchivedGuardRule(5.1),
            AdaptiveLanePlanRuleSequenceRangeRule(32, 64),
            AdaptiveLanePlanRuleCategoryLengthRule(16),
        ),
    )
}
