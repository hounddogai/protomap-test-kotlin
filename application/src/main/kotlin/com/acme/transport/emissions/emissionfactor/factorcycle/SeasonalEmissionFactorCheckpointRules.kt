package com.acme.transport.emissions.emissionfactor.factorcycle

/** A rule a seasonal emission factor checkpoint must pass. Check returns its problems, or none. */
interface SeasonalEmissionFactorCheckpointRule {
    fun check(item: SeasonalEmissionFactorCheckpoint): MutableList<String>
}

data class SeasonalEmissionFactorCheckpointLabelLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorCheckpointRule {
    override fun check(item: SeasonalEmissionFactorCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorCheckpointRequiredCodeRule(
    var message: String,
) : SeasonalEmissionFactorCheckpointRule {
    override fun check(item: SeasonalEmissionFactorCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorCheckpointRequiredLaneRule(
    var message: String,
) : SeasonalEmissionFactorCheckpointRule {
    override fun check(item: SeasonalEmissionFactorCheckpoint): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorCheckpointLaneLengthRule(
    var maxLength: Int,
) : SeasonalEmissionFactorCheckpointRule {
    override fun check(item: SeasonalEmissionFactorCheckpoint): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal emission factor checkpoint. */
data class SeasonalEmissionFactorCheckpointValidator(
    var rules: MutableList<SeasonalEmissionFactorCheckpointRule>,
) {
    fun validate(item: SeasonalEmissionFactorCheckpoint): MutableList<String> {
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

    fun requireValid(item: SeasonalEmissionFactorCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEmissionFactorCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEmissionFactorCheckpointValidator(): SeasonalEmissionFactorCheckpointValidator {
    return SeasonalEmissionFactorCheckpointValidator(
        mutableListOf<SeasonalEmissionFactorCheckpointRule>(
            SeasonalEmissionFactorCheckpointLabelLengthRule(32),
            SeasonalEmissionFactorCheckpointRequiredCodeRule(
                "seasonal emission factor checkpoint needs a value",
            ),
            SeasonalEmissionFactorCheckpointRequiredLaneRule(
                "seasonal emission factor checkpoint needs this field",
            ),
            SeasonalEmissionFactorCheckpointLaneLengthRule(16),
        ),
    )
}
