package com.acme.transport.linehaul.capacity.capacityallocation

/** A rule a legacy capacity snapshot must pass. Check returns its problems, or none. */
interface LegacyCapacitySnapshotRule {
    fun check(item: LegacyCapacitySnapshot): MutableList<String>
}

data class LegacyCapacitySnapshotLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCapacitySnapshotRule {
    override fun check(item: LegacyCapacitySnapshot): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCapacitySnapshotSummaryLengthRule(var maxLength: Int) : LegacyCapacitySnapshotRule {
    override fun check(item: LegacyCapacitySnapshot): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCapacitySnapshotRequiredNameRule(var message: String) : LegacyCapacitySnapshotRule {
    override fun check(item: LegacyCapacitySnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy capacity snapshot. */
data class LegacyCapacitySnapshotValidator(var rules: MutableList<LegacyCapacitySnapshotRule>) {
    fun validate(item: LegacyCapacitySnapshot): MutableList<String> {
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

    fun requireValid(item: LegacyCapacitySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCapacitySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCapacitySnapshotValidator(): LegacyCapacitySnapshotValidator {
    return LegacyCapacitySnapshotValidator(
        mutableListOf<LegacyCapacitySnapshotRule>(
            LegacyCapacitySnapshotLoadKgRangeRule(5.6, 2245.6),
            LegacyCapacitySnapshotSummaryLengthRule(3600),
            LegacyCapacitySnapshotRequiredNameRule("legacy capacity snapshot needs a value"),
        ),
    )
}
