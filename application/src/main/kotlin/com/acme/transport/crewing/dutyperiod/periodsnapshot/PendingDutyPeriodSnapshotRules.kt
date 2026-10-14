package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.kit.text.normalize

/** A rule a pending duty period snapshot must pass. Check returns its problems, or none. */
interface PendingDutyPeriodSnapshotRule {
    fun check(item: PendingDutyPeriodSnapshot): MutableList<String>
}

data class PendingDutyPeriodSnapshotEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingDutyPeriodSnapshotRule {
    override fun check(item: PendingDutyPeriodSnapshot): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodSnapshotRateRangeRule(
    var min: Double,
    var max: Double,
) : PendingDutyPeriodSnapshotRule {
    override fun check(item: PendingDutyPeriodSnapshot): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class PendingDutyPeriodSnapshotDriverIdLengthRule(var maxLength: Int) : PendingDutyPeriodSnapshotRule {
    override fun check(item: PendingDutyPeriodSnapshot): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodSnapshotAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingDutyPeriodSnapshotRule {
    override fun check(item: PendingDutyPeriodSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodSnapshotCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingDutyPeriodSnapshotRule {
    override fun check(item: PendingDutyPeriodSnapshot): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending duty period snapshot. */
data class PendingDutyPeriodSnapshotValidator(var rules: MutableList<PendingDutyPeriodSnapshotRule>) {
    fun validate(item: PendingDutyPeriodSnapshot): MutableList<String> {
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

    fun requireValid(item: PendingDutyPeriodSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDutyPeriodSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDutyPeriodSnapshotValidator(): PendingDutyPeriodSnapshotValidator {
    return PendingDutyPeriodSnapshotValidator(
        mutableListOf<PendingDutyPeriodSnapshotRule>(
            PendingDutyPeriodSnapshotEndsAtRangeRule(1000, 86400),
            PendingDutyPeriodSnapshotRateRangeRule(5.1, 2940.9),
            PendingDutyPeriodSnapshotDriverIdLengthRule(1000),
            PendingDutyPeriodSnapshotAllowedCodeRule(mutableListOf<String>("primary", "seasonal")),
            PendingDutyPeriodSnapshotCreatedAtRangeRule(120, 3600),
        ),
    )
}
