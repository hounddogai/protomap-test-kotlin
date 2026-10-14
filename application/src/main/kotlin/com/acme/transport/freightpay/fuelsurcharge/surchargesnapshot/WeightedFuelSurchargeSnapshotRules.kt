package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

/** A rule a weighted fuel surcharge snapshot must pass. Check returns its problems, or none. */
interface WeightedFuelSurchargeSnapshotRule {
    fun check(item: WeightedFuelSurchargeSnapshot): MutableList<String>
}

data class WeightedFuelSurchargeSnapshotDescriptionLengthRule(
    var maxLength: Int,
) : WeightedFuelSurchargeSnapshotRule {
    override fun check(item: WeightedFuelSurchargeSnapshot): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeSnapshotCodePrefixRule(var prefix: String) : WeightedFuelSurchargeSnapshotRule {
    override fun check(item: WeightedFuelSurchargeSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeSnapshotCapacityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFuelSurchargeSnapshotRule {
    override fun check(item: WeightedFuelSurchargeSnapshot): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeSnapshotCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFuelSurchargeSnapshotRule {
    override fun check(item: WeightedFuelSurchargeSnapshot): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted fuel surcharge snapshot. */
data class WeightedFuelSurchargeSnapshotValidator(var rules: MutableList<WeightedFuelSurchargeSnapshotRule>) {
    fun validate(item: WeightedFuelSurchargeSnapshot): MutableList<String> {
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

    fun requireValid(item: WeightedFuelSurchargeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFuelSurchargeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFuelSurchargeSnapshotValidator(): WeightedFuelSurchargeSnapshotValidator {
    return WeightedFuelSurchargeSnapshotValidator(
        mutableListOf<WeightedFuelSurchargeSnapshotRule>(
            WeightedFuelSurchargeSnapshotDescriptionLengthRule(255),
            WeightedFuelSurchargeSnapshotCodePrefixRule("Q"),
            WeightedFuelSurchargeSnapshotCapacityRangeRule(16, 86400),
            WeightedFuelSurchargeSnapshotCreatedAtRangeRule(16, 120),
        ),
    )
}
