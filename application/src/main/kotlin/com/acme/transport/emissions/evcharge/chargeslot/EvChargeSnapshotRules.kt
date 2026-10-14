package com.acme.transport.emissions.evcharge.chargeslot

/** A rule a ev charge snapshot must pass. Check returns its problems, or none. */
interface EvChargeSnapshotRule {
    fun check(item: EvChargeSnapshot): MutableList<String>
}

data class EvChargeSnapshotRequiredLabelRule(var message: String) : EvChargeSnapshotRule {
    override fun check(item: EvChargeSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSnapshotFuelLitersRangeRule(var min: Double, var max: Double) : EvChargeSnapshotRule {
    override fun check(item: EvChargeSnapshot): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class EvChargeSnapshotCodeLengthRule(var maxLength: Int) : EvChargeSnapshotRule {
    override fun check(item: EvChargeSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSnapshotRequiredCodeRule(var message: String) : EvChargeSnapshotRule {
    override fun check(item: EvChargeSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSnapshotReviewerLengthRule(var maxLength: Int) : EvChargeSnapshotRule {
    override fun check(item: EvChargeSnapshot): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSnapshotDurationMinutesRangeRule(var min: Int, var max: Int) : EvChargeSnapshotRule {
    override fun check(item: EvChargeSnapshot): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a ev charge snapshot. */
data class EvChargeSnapshotValidator(var rules: MutableList<EvChargeSnapshotRule>) {
    fun validate(item: EvChargeSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeSnapshotValidator(): EvChargeSnapshotValidator {
    return EvChargeSnapshotValidator(
        mutableListOf<EvChargeSnapshotRule>(
            EvChargeSnapshotRequiredLabelRule("ev charge snapshot needs this field"),
            EvChargeSnapshotFuelLitersRangeRule(8.3, 2677.7),
            EvChargeSnapshotCodeLengthRule(1000),
            EvChargeSnapshotRequiredCodeRule("ev charge snapshot needs a value"),
            EvChargeSnapshotReviewerLengthRule(1000),
            EvChargeSnapshotDurationMinutesRangeRule(16, 80),
        ),
    )
}
