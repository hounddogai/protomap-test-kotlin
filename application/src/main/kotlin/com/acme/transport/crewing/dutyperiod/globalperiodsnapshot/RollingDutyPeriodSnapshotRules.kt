package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

/** A rule a rolling duty period snapshot must pass. Check returns its problems, or none. */
interface RollingDutyPeriodSnapshotRule {
    fun check(item: RollingDutyPeriodSnapshot): MutableList<String>
}

data class RollingDutyPeriodSnapshotNameLengthRule(var maxLength: Int) : RollingDutyPeriodSnapshotRule {
    override fun check(item: RollingDutyPeriodSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodSnapshotLocaleLengthRule(var maxLength: Int) : RollingDutyPeriodSnapshotRule {
    override fun check(item: RollingDutyPeriodSnapshot): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodSnapshotRequiredNameRule(var message: String) : RollingDutyPeriodSnapshotRule {
    override fun check(item: RollingDutyPeriodSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodSnapshotScoreRangeRule(
    var min: Double,
    var max: Double,
) : RollingDutyPeriodSnapshotRule {
    override fun check(item: RollingDutyPeriodSnapshot): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodSnapshotCodePrefixRule(var prefix: String) : RollingDutyPeriodSnapshotRule {
    override fun check(item: RollingDutyPeriodSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling duty period snapshot. */
data class RollingDutyPeriodSnapshotValidator(var rules: MutableList<RollingDutyPeriodSnapshotRule>) {
    fun validate(item: RollingDutyPeriodSnapshot): MutableList<String> {
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

    fun requireValid(item: RollingDutyPeriodSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDutyPeriodSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDutyPeriodSnapshotValidator(): RollingDutyPeriodSnapshotValidator {
    return RollingDutyPeriodSnapshotValidator(
        mutableListOf<RollingDutyPeriodSnapshotRule>(
            RollingDutyPeriodSnapshotNameLengthRule(1000),
            RollingDutyPeriodSnapshotLocaleLengthRule(255),
            RollingDutyPeriodSnapshotRequiredNameRule("rolling duty period snapshot needs an entry"),
            RollingDutyPeriodSnapshotScoreRangeRule(5.2, 386.8),
            RollingDutyPeriodSnapshotCodePrefixRule("REF"),
        ),
    )
}
