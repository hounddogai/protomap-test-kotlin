package com.acme.transport.safety.coaching.baseline

/** A rule a regional coaching checkpoint must pass. Check returns its problems, or none. */
interface RegionalCoachingCheckpointRule {
    fun check(item: RegionalCoachingCheckpoint): MutableList<String>
}

data class RegionalCoachingCheckpointRequiredCodeRule(var message: String) : RegionalCoachingCheckpointRule {
    override fun check(item: RegionalCoachingCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingCheckpointNotesLengthRule(var maxLength: Int) : RegionalCoachingCheckpointRule {
    override fun check(item: RegionalCoachingCheckpoint): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingCheckpointCodeLengthRule(var maxLength: Int) : RegionalCoachingCheckpointRule {
    override fun check(item: RegionalCoachingCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingCheckpointFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCoachingCheckpointRule {
    override fun check(item: RegionalCoachingCheckpoint): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingCheckpointActiveGuardRule(var limit: Int) : RegionalCoachingCheckpointRule {
    override fun check(item: RegionalCoachingCheckpoint): MutableList<String> {
        if (item.active && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingCheckpointShiftCodeLengthRule(var maxLength: Int) : RegionalCoachingCheckpointRule {
    override fun check(item: RegionalCoachingCheckpoint): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional coaching checkpoint. */
data class RegionalCoachingCheckpointValidator(var rules: MutableList<RegionalCoachingCheckpointRule>) {
    fun validate(item: RegionalCoachingCheckpoint): MutableList<String> {
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

    fun requireValid(item: RegionalCoachingCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCoachingCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCoachingCheckpointValidator(): RegionalCoachingCheckpointValidator {
    return RegionalCoachingCheckpointValidator(
        mutableListOf<RegionalCoachingCheckpointRule>(
            RegionalCoachingCheckpointRequiredCodeRule("regional coaching checkpoint needs an entry"),
            RegionalCoachingCheckpointNotesLengthRule(80),
            RegionalCoachingCheckpointCodeLengthRule(16),
            RegionalCoachingCheckpointFuelLitersRangeRule(5.7, 4499.9),
            RegionalCoachingCheckpointActiveGuardRule(32),
            RegionalCoachingCheckpointShiftCodeLengthRule(86400),
        ),
    )
}
