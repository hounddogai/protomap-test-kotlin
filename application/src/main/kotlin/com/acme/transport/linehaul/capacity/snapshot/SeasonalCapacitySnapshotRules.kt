package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a seasonal capacity snapshot must pass. Check returns its problems, or none. */
interface SeasonalCapacitySnapshotRule {
    fun check(item: SeasonalCapacitySnapshot): MutableList<String>
}

data class SeasonalCapacitySnapshotAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalCapacitySnapshotRule {
    override fun check(item: SeasonalCapacitySnapshot): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class SeasonalCapacitySnapshotChecksumLengthRule(var maxLength: Int) : SeasonalCapacitySnapshotRule {
    override fun check(item: SeasonalCapacitySnapshot): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacitySnapshotDepotLengthRule(var maxLength: Int) : SeasonalCapacitySnapshotRule {
    override fun check(item: SeasonalCapacitySnapshot): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacitySnapshotWeightRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalCapacitySnapshotRule {
    override fun check(item: SeasonalCapacitySnapshot): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class SeasonalCapacitySnapshotAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : SeasonalCapacitySnapshotRule {
    override fun check(item: SeasonalCapacitySnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal capacity snapshot. */
data class SeasonalCapacitySnapshotValidator(var rules: MutableList<SeasonalCapacitySnapshotRule>) {
    fun validate(item: SeasonalCapacitySnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCapacitySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCapacitySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCapacitySnapshotValidator(): SeasonalCapacitySnapshotValidator {
    return SeasonalCapacitySnapshotValidator(
        mutableListOf<SeasonalCapacitySnapshotRule>(
            SeasonalCapacitySnapshotAttemptsRangeRule(64, 120),
            SeasonalCapacitySnapshotChecksumLengthRule(1000),
            SeasonalCapacitySnapshotDepotLengthRule(1000),
            SeasonalCapacitySnapshotWeightRangeRule(6.0, 4949.2),
            SeasonalCapacitySnapshotAllowedContactEmailRule(
                mutableListOf<String>("rolling", "rolling", "fallback"),
            ),
        ),
    )
}
