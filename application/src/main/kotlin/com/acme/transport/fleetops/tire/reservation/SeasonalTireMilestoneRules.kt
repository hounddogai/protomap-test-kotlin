package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.text.normalize

/** A rule a seasonal tire milestone must pass. Check returns its problems, or none. */
interface SeasonalTireMilestoneRule {
    fun check(item: SeasonalTireMilestone): MutableList<String>
}

data class SeasonalTireMilestoneAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : SeasonalTireMilestoneRule {
    override fun check(item: SeasonalTireMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireMilestoneRequiredSourceRule(var message: String) : SeasonalTireMilestoneRule {
    override fun check(item: SeasonalTireMilestone): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireMilestoneRequiredNameRule(var message: String) : SeasonalTireMilestoneRule {
    override fun check(item: SeasonalTireMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireMilestoneCodeLengthRule(var maxLength: Int) : SeasonalTireMilestoneRule {
    override fun check(item: SeasonalTireMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal tire milestone. */
data class SeasonalTireMilestoneValidator(var rules: MutableList<SeasonalTireMilestoneRule>) {
    fun validate(item: SeasonalTireMilestone): MutableList<String> {
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

    fun requireValid(item: SeasonalTireMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTireMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTireMilestoneValidator(): SeasonalTireMilestoneValidator {
    return SeasonalTireMilestoneValidator(
        mutableListOf<SeasonalTireMilestoneRule>(
            SeasonalTireMilestoneAllowedVehicleIdRule(mutableListOf<String>("pending", "rolling")),
            SeasonalTireMilestoneRequiredSourceRule("seasonal tire milestone needs this field"),
            SeasonalTireMilestoneRequiredNameRule("seasonal tire milestone needs an entry"),
            SeasonalTireMilestoneCodeLengthRule(3600),
        ),
    )
}
