package com.acme.transport.safety.coaching.snapshot

/** A rule a scheduled coaching snapshot must pass. Check returns its problems, or none. */
interface ScheduledCoachingSnapshotRule {
    fun check(item: ScheduledCoachingSnapshot): MutableList<String>
}

data class ScheduledCoachingSnapshotCodeLengthRule(var maxLength: Int) : ScheduledCoachingSnapshotRule {
    override fun check(item: ScheduledCoachingSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingSnapshotRequiredVehicleIdRule(var message: String) : ScheduledCoachingSnapshotRule {
    override fun check(item: ScheduledCoachingSnapshot): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingSnapshotRateRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledCoachingSnapshotRule {
    override fun check(item: ScheduledCoachingSnapshot): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingSnapshotBudgetRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledCoachingSnapshotRule {
    override fun check(item: ScheduledCoachingSnapshot): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ScheduledCoachingSnapshotEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledCoachingSnapshotRule {
    override fun check(item: ScheduledCoachingSnapshot): MutableList<String> {
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

/** Runs every rule of a scheduled coaching snapshot. */
data class ScheduledCoachingSnapshotValidator(var rules: MutableList<ScheduledCoachingSnapshotRule>) {
    fun validate(item: ScheduledCoachingSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledCoachingSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCoachingSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCoachingSnapshotValidator(): ScheduledCoachingSnapshotValidator {
    return ScheduledCoachingSnapshotValidator(
        mutableListOf<ScheduledCoachingSnapshotRule>(
            ScheduledCoachingSnapshotCodeLengthRule(80),
            ScheduledCoachingSnapshotRequiredVehicleIdRule("scheduled coaching snapshot needs an entry"),
            ScheduledCoachingSnapshotRateRangeRule(7.6, 2396.2),
            ScheduledCoachingSnapshotBudgetRangeRule(5.6, 3306.9),
            ScheduledCoachingSnapshotEtaMinutesRangeRule(64, 120),
        ),
    )
}
