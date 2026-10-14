package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.text.normalize

/** A rule a strict ev charge snapshot must pass. Check returns its problems, or none. */
interface StrictEvChargeSnapshotRule {
    fun check(item: StrictEvChargeSnapshot): MutableList<String>
}

data class StrictEvChargeSnapshotNameLengthRule(var maxLength: Int) : StrictEvChargeSnapshotRule {
    override fun check(item: StrictEvChargeSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSnapshotRequiredCodeRule(var message: String) : StrictEvChargeSnapshotRule {
    override fun check(item: StrictEvChargeSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSnapshotAllowedUnitRule(
    var allowed: MutableList<String>,
) : StrictEvChargeSnapshotRule {
    override fun check(item: StrictEvChargeSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSnapshotBatchSizeRangeRule(var min: Int, var max: Int) : StrictEvChargeSnapshotRule {
    override fun check(item: StrictEvChargeSnapshot): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class StrictEvChargeSnapshotShiftCodeLengthRule(var maxLength: Int) : StrictEvChargeSnapshotRule {
    override fun check(item: StrictEvChargeSnapshot): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict ev charge snapshot. */
data class StrictEvChargeSnapshotValidator(var rules: MutableList<StrictEvChargeSnapshotRule>) {
    fun validate(item: StrictEvChargeSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictEvChargeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEvChargeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEvChargeSnapshotValidator(): StrictEvChargeSnapshotValidator {
    return StrictEvChargeSnapshotValidator(
        mutableListOf<StrictEvChargeSnapshotRule>(
            StrictEvChargeSnapshotNameLengthRule(120),
            StrictEvChargeSnapshotRequiredCodeRule("strict ev charge snapshot needs an entry"),
            StrictEvChargeSnapshotAllowedUnitRule(mutableListOf<String>()),
            StrictEvChargeSnapshotBatchSizeRangeRule(32, 86400),
            StrictEvChargeSnapshotShiftCodeLengthRule(1000),
        ),
    )
}
