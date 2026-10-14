package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.text.normalize

/** A rule a manual duty period snapshot must pass. Check returns its problems, or none. */
interface ManualDutyPeriodSnapshotRule {
    fun check(item: ManualDutyPeriodSnapshot): MutableList<String>
}

data class ManualDutyPeriodSnapshotCodePrefixRule(var prefix: String) : ManualDutyPeriodSnapshotRule {
    override fun check(item: ManualDutyPeriodSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodSnapshotExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualDutyPeriodSnapshotRule {
    override fun check(item: ManualDutyPeriodSnapshot): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodSnapshotRequiredSegmentRule(var message: String) : ManualDutyPeriodSnapshotRule {
    override fun check(item: ManualDutyPeriodSnapshot): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodSnapshotAllowedCodeRule(
    var allowed: MutableList<String>,
) : ManualDutyPeriodSnapshotRule {
    override fun check(item: ManualDutyPeriodSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodSnapshotNameLengthRule(var maxLength: Int) : ManualDutyPeriodSnapshotRule {
    override fun check(item: ManualDutyPeriodSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual duty period snapshot. */
data class ManualDutyPeriodSnapshotValidator(var rules: MutableList<ManualDutyPeriodSnapshotRule>) {
    fun validate(item: ManualDutyPeriodSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDutyPeriodSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDutyPeriodSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDutyPeriodSnapshotValidator(): ManualDutyPeriodSnapshotValidator {
    return ManualDutyPeriodSnapshotValidator(
        mutableListOf<ManualDutyPeriodSnapshotRule>(
            ManualDutyPeriodSnapshotCodePrefixRule("REF"),
            ManualDutyPeriodSnapshotExpiresAtRangeRule(32, 86400),
            ManualDutyPeriodSnapshotRequiredSegmentRule("manual duty period snapshot needs this field"),
            ManualDutyPeriodSnapshotAllowedCodeRule(mutableListOf<String>()),
            ManualDutyPeriodSnapshotNameLengthRule(64),
        ),
    )
}
