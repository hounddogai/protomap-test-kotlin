package com.acme.transport.crewing.dutyperiod.periodthreshold

/** A rule a capped duty period adjustment must pass. Check returns its problems, or none. */
interface CappedDutyPeriodAdjustmentRule {
    fun check(item: CappedDutyPeriodAdjustment): MutableList<String>
}

data class CappedDutyPeriodAdjustmentVisibleGuardRule(var limit: Int) : CappedDutyPeriodAdjustmentRule {
    override fun check(item: CappedDutyPeriodAdjustment): MutableList<String> {
        if (item.visible && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class CappedDutyPeriodAdjustmentChannelLengthRule(var maxLength: Int) : CappedDutyPeriodAdjustmentRule {
    override fun check(item: CappedDutyPeriodAdjustment): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDutyPeriodAdjustmentNameLengthRule(var maxLength: Int) : CappedDutyPeriodAdjustmentRule {
    override fun check(item: CappedDutyPeriodAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDutyPeriodAdjustmentRequiredNameRule(var message: String) : CappedDutyPeriodAdjustmentRule {
    override fun check(item: CappedDutyPeriodAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDutyPeriodAdjustmentRequiredOwnerIdRule(var message: String) : CappedDutyPeriodAdjustmentRule {
    override fun check(item: CappedDutyPeriodAdjustment): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped duty period adjustment. */
data class CappedDutyPeriodAdjustmentValidator(var rules: MutableList<CappedDutyPeriodAdjustmentRule>) {
    fun validate(item: CappedDutyPeriodAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedDutyPeriodAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDutyPeriodAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDutyPeriodAdjustmentValidator(): CappedDutyPeriodAdjustmentValidator {
    return CappedDutyPeriodAdjustmentValidator(
        mutableListOf<CappedDutyPeriodAdjustmentRule>(
            CappedDutyPeriodAdjustmentVisibleGuardRule(1000),
            CappedDutyPeriodAdjustmentChannelLengthRule(80),
            CappedDutyPeriodAdjustmentNameLengthRule(120),
            CappedDutyPeriodAdjustmentRequiredNameRule("capped duty period adjustment needs an entry"),
            CappedDutyPeriodAdjustmentRequiredOwnerIdRule("capped duty period adjustment needs an entry"),
        ),
    )
}
