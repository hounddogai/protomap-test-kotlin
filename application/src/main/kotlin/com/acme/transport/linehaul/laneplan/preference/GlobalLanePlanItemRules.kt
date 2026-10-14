package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.text.normalize

/** A rule a global lane plan item must pass. Check returns its problems, or none. */
interface GlobalLanePlanItemRule {
    fun check(item: GlobalLanePlanItem): MutableList<String>
}

data class GlobalLanePlanItemAllowedTrailerIdRule(var allowed: MutableList<String>) : GlobalLanePlanItemRule {
    override fun check(item: GlobalLanePlanItem): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLanePlanItemRequiredReviewerRule(var message: String) : GlobalLanePlanItemRule {
    override fun check(item: GlobalLanePlanItem): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLanePlanItemRequiredTrailerIdRule(var message: String) : GlobalLanePlanItemRule {
    override fun check(item: GlobalLanePlanItem): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLanePlanItemRequiredTitleRule(var message: String) : GlobalLanePlanItemRule {
    override fun check(item: GlobalLanePlanItem): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLanePlanItemEtaMinutesRangeRule(var min: Int, var max: Int) : GlobalLanePlanItemRule {
    override fun check(item: GlobalLanePlanItem): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global lane plan item. */
data class GlobalLanePlanItemValidator(var rules: MutableList<GlobalLanePlanItemRule>) {
    fun validate(item: GlobalLanePlanItem): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalLanePlanItem) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalLanePlanItem>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalLanePlanItemValidator(): GlobalLanePlanItemValidator {
    return GlobalLanePlanItemValidator(
        mutableListOf<GlobalLanePlanItemRule>(
            GlobalLanePlanItemAllowedTrailerIdRule(mutableListOf<String>()),
            GlobalLanePlanItemRequiredReviewerRule("global lane plan item needs an entry"),
            GlobalLanePlanItemRequiredTrailerIdRule("global lane plan item needs this field"),
            GlobalLanePlanItemRequiredTitleRule("global lane plan item needs an entry"),
            GlobalLanePlanItemEtaMinutesRangeRule(64, 86400),
        ),
    )
}
