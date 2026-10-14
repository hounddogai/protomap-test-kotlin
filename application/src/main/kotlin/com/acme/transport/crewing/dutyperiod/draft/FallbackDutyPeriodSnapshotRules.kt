package com.acme.transport.crewing.dutyperiod.draft

/** A rule a fallback duty period snapshot must pass. Check returns its problems, or none. */
interface FallbackDutyPeriodSnapshotRule {
    fun check(item: FallbackDutyPeriodSnapshot): MutableList<String>
}

data class FallbackDutyPeriodSnapshotTitleLengthRule(var maxLength: Int) : FallbackDutyPeriodSnapshotRule {
    override fun check(item: FallbackDutyPeriodSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDutyPeriodSnapshotLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : FallbackDutyPeriodSnapshotRule {
    override fun check(item: FallbackDutyPeriodSnapshot): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class FallbackDutyPeriodSnapshotLabelLengthRule(var maxLength: Int) : FallbackDutyPeriodSnapshotRule {
    override fun check(item: FallbackDutyPeriodSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDutyPeriodSnapshotMarginRangeRule(
    var min: Double,
    var max: Double,
) : FallbackDutyPeriodSnapshotRule {
    override fun check(item: FallbackDutyPeriodSnapshot): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback duty period snapshot. */
data class FallbackDutyPeriodSnapshotValidator(var rules: MutableList<FallbackDutyPeriodSnapshotRule>) {
    fun validate(item: FallbackDutyPeriodSnapshot): MutableList<String> {
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

    fun requireValid(item: FallbackDutyPeriodSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDutyPeriodSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDutyPeriodSnapshotValidator(): FallbackDutyPeriodSnapshotValidator {
    return FallbackDutyPeriodSnapshotValidator(
        mutableListOf<FallbackDutyPeriodSnapshotRule>(
            FallbackDutyPeriodSnapshotTitleLengthRule(80),
            FallbackDutyPeriodSnapshotLoadKgRangeRule(7.3, 1915.3),
            FallbackDutyPeriodSnapshotLabelLengthRule(32),
            FallbackDutyPeriodSnapshotMarginRangeRule(2.6, 4167.6),
        ),
    )
}
