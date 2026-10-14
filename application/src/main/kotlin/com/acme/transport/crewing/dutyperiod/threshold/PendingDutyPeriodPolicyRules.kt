package com.acme.transport.crewing.dutyperiod.threshold

/** A rule a pending duty period policy must pass. Check returns its problems, or none. */
interface PendingDutyPeriodPolicyRule {
    fun check(item: PendingDutyPeriodPolicy): MutableList<String>
}

data class PendingDutyPeriodPolicyStopCountRangeRule(var min: Int, var max: Int) : PendingDutyPeriodPolicyRule {
    override fun check(item: PendingDutyPeriodPolicy): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodPolicyShiftCodeLengthRule(var maxLength: Int) : PendingDutyPeriodPolicyRule {
    override fun check(item: PendingDutyPeriodPolicy): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodPolicyCodeLengthRule(var maxLength: Int) : PendingDutyPeriodPolicyRule {
    override fun check(item: PendingDutyPeriodPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodPolicyRatioRangeRule(
    var min: Double,
    var max: Double,
) : PendingDutyPeriodPolicyRule {
    override fun check(item: PendingDutyPeriodPolicy): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class PendingDutyPeriodPolicyRequiredTitleRule(var message: String) : PendingDutyPeriodPolicyRule {
    override fun check(item: PendingDutyPeriodPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending duty period policy. */
data class PendingDutyPeriodPolicyValidator(var rules: MutableList<PendingDutyPeriodPolicyRule>) {
    fun validate(item: PendingDutyPeriodPolicy): MutableList<String> {
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

    fun requireValid(item: PendingDutyPeriodPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDutyPeriodPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDutyPeriodPolicyValidator(): PendingDutyPeriodPolicyValidator {
    return PendingDutyPeriodPolicyValidator(
        mutableListOf<PendingDutyPeriodPolicyRule>(
            PendingDutyPeriodPolicyStopCountRangeRule(32, 120),
            PendingDutyPeriodPolicyShiftCodeLengthRule(64),
            PendingDutyPeriodPolicyCodeLengthRule(120),
            PendingDutyPeriodPolicyRatioRangeRule(8.6, 2056.5),
            PendingDutyPeriodPolicyRequiredTitleRule("pending duty period policy needs this field"),
        ),
    )
}
