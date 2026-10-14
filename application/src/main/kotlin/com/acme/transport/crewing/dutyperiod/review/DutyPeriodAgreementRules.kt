package com.acme.transport.crewing.dutyperiod.review

/** A rule a duty period agreement must pass. Check returns its problems, or none. */
interface DutyPeriodAgreementRule {
    fun check(item: DutyPeriodAgreement): MutableList<String>
}

data class DutyPeriodAgreementDriverIdLengthRule(var maxLength: Int) : DutyPeriodAgreementRule {
    override fun check(item: DutyPeriodAgreement): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAgreementCapacityRangeRule(var min: Int, var max: Int) : DutyPeriodAgreementRule {
    override fun check(item: DutyPeriodAgreement): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodAgreementVisibleGuardRule(var limit: Int) : DutyPeriodAgreementRule {
    override fun check(item: DutyPeriodAgreement): MutableList<String> {
        if (item.visible && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAgreementRequiredLabelRule(var message: String) : DutyPeriodAgreementRule {
    override fun check(item: DutyPeriodAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAgreementLabelLengthRule(var maxLength: Int) : DutyPeriodAgreementRule {
    override fun check(item: DutyPeriodAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period agreement. */
data class DutyPeriodAgreementValidator(var rules: MutableList<DutyPeriodAgreementRule>) {
    fun validate(item: DutyPeriodAgreement): MutableList<String> {
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

    fun requireValid(item: DutyPeriodAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodAgreementValidator(): DutyPeriodAgreementValidator {
    return DutyPeriodAgreementValidator(
        mutableListOf<DutyPeriodAgreementRule>(
            DutyPeriodAgreementDriverIdLengthRule(1000),
            DutyPeriodAgreementCapacityRangeRule(16, 64),
            DutyPeriodAgreementVisibleGuardRule(86400),
            DutyPeriodAgreementRequiredLabelRule("duty period agreement needs an entry"),
            DutyPeriodAgreementLabelLengthRule(255),
        ),
    )
}
