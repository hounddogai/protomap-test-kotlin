package com.acme.transport.crewing.dutyperiod.request

/** A rule a default duty period snapshot must pass. Check returns its problems, or none. */
interface DefaultDutyPeriodSnapshotRule {
    fun check(item: DefaultDutyPeriodSnapshot): MutableList<String>
}

data class DefaultDutyPeriodSnapshotRequiredLabelRule(var message: String) : DefaultDutyPeriodSnapshotRule {
    override fun check(item: DefaultDutyPeriodSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodSnapshotRequiredDepotRule(var message: String) : DefaultDutyPeriodSnapshotRule {
    override fun check(item: DefaultDutyPeriodSnapshot): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodSnapshotCodeLengthRule(var maxLength: Int) : DefaultDutyPeriodSnapshotRule {
    override fun check(item: DefaultDutyPeriodSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodSnapshotBudgetRangeRule(
    var min: Double,
    var max: Double,
) : DefaultDutyPeriodSnapshotRule {
    override fun check(item: DefaultDutyPeriodSnapshot): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class DefaultDutyPeriodSnapshotTimezoneLengthRule(var maxLength: Int) : DefaultDutyPeriodSnapshotRule {
    override fun check(item: DefaultDutyPeriodSnapshot): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodSnapshotRequiredCodeRule(var message: String) : DefaultDutyPeriodSnapshotRule {
    override fun check(item: DefaultDutyPeriodSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default duty period snapshot. */
data class DefaultDutyPeriodSnapshotValidator(var rules: MutableList<DefaultDutyPeriodSnapshotRule>) {
    fun validate(item: DefaultDutyPeriodSnapshot): MutableList<String> {
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

    fun requireValid(item: DefaultDutyPeriodSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDutyPeriodSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDutyPeriodSnapshotValidator(): DefaultDutyPeriodSnapshotValidator {
    return DefaultDutyPeriodSnapshotValidator(
        mutableListOf<DefaultDutyPeriodSnapshotRule>(
            DefaultDutyPeriodSnapshotRequiredLabelRule("default duty period snapshot needs this field"),
            DefaultDutyPeriodSnapshotRequiredDepotRule("default duty period snapshot needs a value"),
            DefaultDutyPeriodSnapshotCodeLengthRule(86400),
            DefaultDutyPeriodSnapshotBudgetRangeRule(1.8, 2045.8),
            DefaultDutyPeriodSnapshotTimezoneLengthRule(120),
            DefaultDutyPeriodSnapshotRequiredCodeRule("default duty period snapshot needs an entry"),
        ),
    )
}
