package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge snapshot must pass. Check returns its problems, or none. */
interface FuelSurchargeSnapshotRule {
    fun check(item: FuelSurchargeSnapshot): MutableList<String>
}

data class FuelSurchargeSnapshotTitleLengthRule(var maxLength: Int) : FuelSurchargeSnapshotRule {
    override fun check(item: FuelSurchargeSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSnapshotAllowedTitleRule(var allowed: MutableList<String>) : FuelSurchargeSnapshotRule {
    override fun check(item: FuelSurchargeSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSnapshotFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : FuelSurchargeSnapshotRule {
    override fun check(item: FuelSurchargeSnapshot): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge snapshot. */
data class FuelSurchargeSnapshotValidator(var rules: MutableList<FuelSurchargeSnapshotRule>) {
    fun validate(item: FuelSurchargeSnapshot): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeSnapshotValidator(): FuelSurchargeSnapshotValidator {
    return FuelSurchargeSnapshotValidator(
        mutableListOf<FuelSurchargeSnapshotRule>(
            FuelSurchargeSnapshotTitleLengthRule(80),
            FuelSurchargeSnapshotAllowedTitleRule(mutableListOf<String>("legacy", "scheduled")),
            FuelSurchargeSnapshotFloorValueRangeRule(9.7, 3187.5),
        ),
    )
}
