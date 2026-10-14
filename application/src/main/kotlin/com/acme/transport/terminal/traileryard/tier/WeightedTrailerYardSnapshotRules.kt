package com.acme.transport.terminal.traileryard.tier

/** A rule a weighted trailer yard snapshot must pass. Check returns its problems, or none. */
interface WeightedTrailerYardSnapshotRule {
    fun check(item: WeightedTrailerYardSnapshot): MutableList<String>
}

data class WeightedTrailerYardSnapshotRequiredVehicleIdRule(
    var message: String,
) : WeightedTrailerYardSnapshotRule {
    override fun check(item: WeightedTrailerYardSnapshot): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardSnapshotUnitLengthRule(var maxLength: Int) : WeightedTrailerYardSnapshotRule {
    override fun check(item: WeightedTrailerYardSnapshot): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardSnapshotBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : WeightedTrailerYardSnapshotRule {
    override fun check(item: WeightedTrailerYardSnapshot): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardSnapshotRequiredTitleRule(var message: String) : WeightedTrailerYardSnapshotRule {
    override fun check(item: WeightedTrailerYardSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardSnapshotTitleLengthRule(var maxLength: Int) : WeightedTrailerYardSnapshotRule {
    override fun check(item: WeightedTrailerYardSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerYardSnapshotQuantityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedTrailerYardSnapshotRule {
    override fun check(item: WeightedTrailerYardSnapshot): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted trailer yard snapshot. */
data class WeightedTrailerYardSnapshotValidator(var rules: MutableList<WeightedTrailerYardSnapshotRule>) {
    fun validate(item: WeightedTrailerYardSnapshot): MutableList<String> {
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

    fun requireValid(item: WeightedTrailerYardSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTrailerYardSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTrailerYardSnapshotValidator(): WeightedTrailerYardSnapshotValidator {
    return WeightedTrailerYardSnapshotValidator(
        mutableListOf<WeightedTrailerYardSnapshotRule>(
            WeightedTrailerYardSnapshotRequiredVehicleIdRule("weighted trailer yard snapshot needs this field"),
            WeightedTrailerYardSnapshotUnitLengthRule(16),
            WeightedTrailerYardSnapshotBatchSizeRangeRule(64, 3600),
            WeightedTrailerYardSnapshotRequiredTitleRule("weighted trailer yard snapshot needs this field"),
            WeightedTrailerYardSnapshotTitleLengthRule(16),
            WeightedTrailerYardSnapshotQuantityRangeRule(80, 86400),
        ),
    )
}
