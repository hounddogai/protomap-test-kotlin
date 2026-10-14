package com.acme.transport.crewing.dutyperiod.notice

import com.acme.transport.kit.text.normalize

/** A rule a duty period notice must pass. Check returns its problems, or none. */
interface DutyPeriodNoticeRule {
    fun check(item: DutyPeriodNotice): MutableList<String>
}

data class DutyPeriodNoticeEndsAtRangeRule(var min: Int, var max: Int) : DutyPeriodNoticeRule {
    override fun check(item: DutyPeriodNotice): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodNoticeCodeLengthRule(var maxLength: Int) : DutyPeriodNoticeRule {
    override fun check(item: DutyPeriodNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodNoticeRateRangeRule(var min: Double, var max: Double) : DutyPeriodNoticeRule {
    override fun check(item: DutyPeriodNotice): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodNoticeRequiredCodeRule(var message: String) : DutyPeriodNoticeRule {
    override fun check(item: DutyPeriodNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodNoticeCapacityRangeRule(var min: Int, var max: Int) : DutyPeriodNoticeRule {
    override fun check(item: DutyPeriodNotice): MutableList<String> {
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

data class DutyPeriodNoticeAllowedLabelRule(var allowed: MutableList<String>) : DutyPeriodNoticeRule {
    override fun check(item: DutyPeriodNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period notice. */
data class DutyPeriodNoticeValidator(var rules: MutableList<DutyPeriodNoticeRule>) {
    fun validate(item: DutyPeriodNotice): MutableList<String> {
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

    fun requireValid(item: DutyPeriodNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodNoticeValidator(): DutyPeriodNoticeValidator {
    return DutyPeriodNoticeValidator(
        mutableListOf<DutyPeriodNoticeRule>(
            DutyPeriodNoticeEndsAtRangeRule(16, 3600),
            DutyPeriodNoticeCodeLengthRule(120),
            DutyPeriodNoticeRateRangeRule(5.2, 2965.2),
            DutyPeriodNoticeRequiredCodeRule("duty period notice needs this field"),
            DutyPeriodNoticeCapacityRangeRule(80, 120),
            DutyPeriodNoticeAllowedLabelRule(mutableListOf<String>("lenient")),
        ),
    )
}
