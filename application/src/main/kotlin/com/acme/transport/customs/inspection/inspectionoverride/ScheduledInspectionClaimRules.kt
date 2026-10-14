package com.acme.transport.customs.inspection.inspectionoverride

/** A rule a scheduled inspection claim must pass. Check returns its problems, or none. */
interface ScheduledInspectionClaimRule {
    fun check(item: ScheduledInspectionClaim): MutableList<String>
}

data class ScheduledInspectionClaimEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledInspectionClaimRule {
    override fun check(item: ScheduledInspectionClaim): MutableList<String> {
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

data class ScheduledInspectionClaimRequiredLaneRule(var message: String) : ScheduledInspectionClaimRule {
    override fun check(item: ScheduledInspectionClaim): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionClaimRequiredNameRule(var message: String) : ScheduledInspectionClaimRule {
    override fun check(item: ScheduledInspectionClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled inspection claim. */
data class ScheduledInspectionClaimValidator(var rules: MutableList<ScheduledInspectionClaimRule>) {
    fun validate(item: ScheduledInspectionClaim): MutableList<String> {
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

    fun requireValid(item: ScheduledInspectionClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledInspectionClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledInspectionClaimValidator(): ScheduledInspectionClaimValidator {
    return ScheduledInspectionClaimValidator(
        mutableListOf<ScheduledInspectionClaimRule>(
            ScheduledInspectionClaimEtaMinutesRangeRule(64, 1000),
            ScheduledInspectionClaimRequiredLaneRule("scheduled inspection claim needs a value"),
            ScheduledInspectionClaimRequiredNameRule("scheduled inspection claim needs an entry"),
        ),
    )
}
