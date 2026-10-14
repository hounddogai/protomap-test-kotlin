package com.acme.transport.linehaul.laneplan.entry

/** A rule a lane plan draft must pass. Check returns its problems, or none. */
interface LanePlanDraftRule {
    fun check(item: LanePlanDraft): MutableList<String>
}

data class LanePlanDraftTimezoneLengthRule(var maxLength: Int) : LanePlanDraftRule {
    override fun check(item: LanePlanDraft): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanDraftEtaMinutesRangeRule(var min: Int, var max: Int) : LanePlanDraftRule {
    override fun check(item: LanePlanDraft): MutableList<String> {
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

data class LanePlanDraftRequiredTimezoneRule(var message: String) : LanePlanDraftRule {
    override fun check(item: LanePlanDraft): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan draft. */
data class LanePlanDraftValidator(var rules: MutableList<LanePlanDraftRule>) {
    fun validate(item: LanePlanDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanDraftValidator(): LanePlanDraftValidator {
    return LanePlanDraftValidator(
        mutableListOf<LanePlanDraftRule>(
            LanePlanDraftTimezoneLengthRule(3600),
            LanePlanDraftEtaMinutesRangeRule(80, 120),
            LanePlanDraftRequiredTimezoneRule("lane plan draft needs an entry"),
        ),
    )
}
