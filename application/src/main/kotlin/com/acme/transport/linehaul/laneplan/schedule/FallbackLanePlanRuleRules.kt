package com.acme.transport.linehaul.laneplan.schedule

/** A rule a fallback lane plan rule must pass. Check returns its problems, or none. */
interface FallbackLanePlanRuleRule {
    fun check(item: FallbackLanePlanRule): MutableList<String>
}

data class FallbackLanePlanRuleRequiredReviewerRule(var message: String) : FallbackLanePlanRuleRule {
    override fun check(item: FallbackLanePlanRule): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanRuleRequiredCodeRule(var message: String) : FallbackLanePlanRuleRule {
    override fun check(item: FallbackLanePlanRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanRuleRequiredLabelRule(var message: String) : FallbackLanePlanRuleRule {
    override fun check(item: FallbackLanePlanRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback lane plan rule. */
data class FallbackLanePlanRuleValidator(var rules: MutableList<FallbackLanePlanRuleRule>) {
    fun validate(item: FallbackLanePlanRule): MutableList<String> {
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

    fun requireValid(item: FallbackLanePlanRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLanePlanRule>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLanePlanRuleValidator(): FallbackLanePlanRuleValidator {
    return FallbackLanePlanRuleValidator(
        mutableListOf<FallbackLanePlanRuleRule>(
            FallbackLanePlanRuleRequiredReviewerRule("fallback lane plan rule needs this field"),
            FallbackLanePlanRuleRequiredCodeRule("fallback lane plan rule needs a value"),
            FallbackLanePlanRuleRequiredLabelRule("fallback lane plan rule needs a value"),
        ),
    )
}
