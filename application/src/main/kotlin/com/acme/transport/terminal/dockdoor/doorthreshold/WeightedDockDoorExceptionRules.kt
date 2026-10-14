package com.acme.transport.terminal.dockdoor.doorthreshold

/** A rule a weighted dock door exception must pass. Check returns its problems, or none. */
interface WeightedDockDoorExceptionRule {
    fun check(item: WeightedDockDoorException): MutableList<String>
}

data class WeightedDockDoorExceptionEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : WeightedDockDoorExceptionRule {
    override fun check(item: WeightedDockDoorException): MutableList<String> {
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

data class WeightedDockDoorExceptionArchivedGuardRule(var limit: Double) : WeightedDockDoorExceptionRule {
    override fun check(item: WeightedDockDoorException): MutableList<String> {
        if (item.archived && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDockDoorExceptionRequiredTitleRule(var message: String) : WeightedDockDoorExceptionRule {
    override fun check(item: WeightedDockDoorException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted dock door exception. */
data class WeightedDockDoorExceptionValidator(var rules: MutableList<WeightedDockDoorExceptionRule>) {
    fun validate(item: WeightedDockDoorException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedDockDoorException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDockDoorException>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDockDoorExceptionValidator(): WeightedDockDoorExceptionValidator {
    return WeightedDockDoorExceptionValidator(
        mutableListOf<WeightedDockDoorExceptionRule>(
            WeightedDockDoorExceptionEtaMinutesRangeRule(16, 120),
            WeightedDockDoorExceptionArchivedGuardRule(1.4),
            WeightedDockDoorExceptionRequiredTitleRule("weighted dock door exception needs this field"),
        ),
    )
}
