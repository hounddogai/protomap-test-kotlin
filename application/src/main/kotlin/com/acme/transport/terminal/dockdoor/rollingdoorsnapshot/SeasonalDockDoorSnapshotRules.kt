package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

/** A rule a seasonal dock door snapshot must pass. Check returns its problems, or none. */
interface SeasonalDockDoorSnapshotRule {
    fun check(item: SeasonalDockDoorSnapshot): MutableList<String>
}

data class SeasonalDockDoorSnapshotRequiredCodeRule(var message: String) : SeasonalDockDoorSnapshotRule {
    override fun check(item: SeasonalDockDoorSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorSnapshotArchivedGuardRule(var limit: Double) : SeasonalDockDoorSnapshotRule {
    override fun check(item: SeasonalDockDoorSnapshot): MutableList<String> {
        if (item.archived && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorSnapshotRequiredTitleRule(var message: String) : SeasonalDockDoorSnapshotRule {
    override fun check(item: SeasonalDockDoorSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorSnapshotRequiredPhoneRule(var message: String) : SeasonalDockDoorSnapshotRule {
    override fun check(item: SeasonalDockDoorSnapshot): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dock door snapshot. */
data class SeasonalDockDoorSnapshotValidator(var rules: MutableList<SeasonalDockDoorSnapshotRule>) {
    fun validate(item: SeasonalDockDoorSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDockDoorSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDockDoorSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDockDoorSnapshotValidator(): SeasonalDockDoorSnapshotValidator {
    return SeasonalDockDoorSnapshotValidator(
        mutableListOf<SeasonalDockDoorSnapshotRule>(
            SeasonalDockDoorSnapshotRequiredCodeRule("seasonal dock door snapshot needs an entry"),
            SeasonalDockDoorSnapshotArchivedGuardRule(4.5),
            SeasonalDockDoorSnapshotRequiredTitleRule("seasonal dock door snapshot needs a value"),
            SeasonalDockDoorSnapshotRequiredPhoneRule("seasonal dock door snapshot needs an entry"),
        ),
    )
}
