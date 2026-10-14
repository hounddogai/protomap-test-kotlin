package com.acme.transport.safety.cameraevent.slot

/** A rule a seasonal camera event plan must pass. Check returns its problems, or none. */
interface SeasonalCameraEventPlanRule {
    fun check(item: SeasonalCameraEventPlan): MutableList<String>
}

data class SeasonalCameraEventPlanRetriesRangeRule(var min: Int, var max: Int) : SeasonalCameraEventPlanRule {
    override fun check(item: SeasonalCameraEventPlan): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class SeasonalCameraEventPlanPinnedGuardRule(var limit: Int) : SeasonalCameraEventPlanRule {
    override fun check(item: SeasonalCameraEventPlan): MutableList<String> {
        if (item.pinned && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCameraEventPlanRequiredTimezoneRule(var message: String) : SeasonalCameraEventPlanRule {
    override fun check(item: SeasonalCameraEventPlan): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal camera event plan. */
data class SeasonalCameraEventPlanValidator(var rules: MutableList<SeasonalCameraEventPlanRule>) {
    fun validate(item: SeasonalCameraEventPlan): MutableList<String> {
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

    fun requireValid(item: SeasonalCameraEventPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCameraEventPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCameraEventPlanValidator(): SeasonalCameraEventPlanValidator {
    return SeasonalCameraEventPlanValidator(
        mutableListOf<SeasonalCameraEventPlanRule>(
            SeasonalCameraEventPlanRetriesRangeRule(64, 255),
            SeasonalCameraEventPlanPinnedGuardRule(32),
            SeasonalCameraEventPlanRequiredTimezoneRule("seasonal camera event plan needs an entry"),
        ),
    )
}
