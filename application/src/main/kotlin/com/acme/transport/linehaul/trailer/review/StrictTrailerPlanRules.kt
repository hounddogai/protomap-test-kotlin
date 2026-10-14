package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.text.normalize

/** A rule a strict trailer plan must pass. Check returns its problems, or none. */
interface StrictTrailerPlanRule {
    fun check(item: StrictTrailerPlan): MutableList<String>
}

data class StrictTrailerPlanRequiredLabelRule(var message: String) : StrictTrailerPlanRule {
    override fun check(item: StrictTrailerPlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerPlanRequiredCodeRule(var message: String) : StrictTrailerPlanRule {
    override fun check(item: StrictTrailerPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerPlanRequiredSegmentRule(var message: String) : StrictTrailerPlanRule {
    override fun check(item: StrictTrailerPlan): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerPlanAllowedLocaleRule(var allowed: MutableList<String>) : StrictTrailerPlanRule {
    override fun check(item: StrictTrailerPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict trailer plan. */
data class StrictTrailerPlanValidator(var rules: MutableList<StrictTrailerPlanRule>) {
    fun validate(item: StrictTrailerPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTrailerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTrailerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTrailerPlanValidator(): StrictTrailerPlanValidator {
    return StrictTrailerPlanValidator(
        mutableListOf<StrictTrailerPlanRule>(
            StrictTrailerPlanRequiredLabelRule("strict trailer plan needs this field"),
            StrictTrailerPlanRequiredCodeRule("strict trailer plan needs a value"),
            StrictTrailerPlanRequiredSegmentRule("strict trailer plan needs an entry"),
            StrictTrailerPlanAllowedLocaleRule(mutableListOf<String>("legacy", "global", "adaptive")),
        ),
    )
}
