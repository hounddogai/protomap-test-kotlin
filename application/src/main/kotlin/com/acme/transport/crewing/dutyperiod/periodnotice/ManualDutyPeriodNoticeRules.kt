package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.text.normalize

/** A rule a manual duty period notice must pass. Check returns its problems, or none. */
interface ManualDutyPeriodNoticeRule {
    fun check(item: ManualDutyPeriodNotice): MutableList<String>
}

data class ManualDutyPeriodNoticeAllowedCodeRule(
    var allowed: MutableList<String>,
) : ManualDutyPeriodNoticeRule {
    override fun check(item: ManualDutyPeriodNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodNoticeCodeLengthRule(var maxLength: Int) : ManualDutyPeriodNoticeRule {
    override fun check(item: ManualDutyPeriodNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodNoticeChannelLengthRule(var maxLength: Int) : ManualDutyPeriodNoticeRule {
    override fun check(item: ManualDutyPeriodNotice): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodNoticeUnitLengthRule(var maxLength: Int) : ManualDutyPeriodNoticeRule {
    override fun check(item: ManualDutyPeriodNotice): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual duty period notice. */
data class ManualDutyPeriodNoticeValidator(var rules: MutableList<ManualDutyPeriodNoticeRule>) {
    fun validate(item: ManualDutyPeriodNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDutyPeriodNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDutyPeriodNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDutyPeriodNoticeValidator(): ManualDutyPeriodNoticeValidator {
    return ManualDutyPeriodNoticeValidator(
        mutableListOf<ManualDutyPeriodNoticeRule>(
            ManualDutyPeriodNoticeAllowedCodeRule(mutableListOf<String>("weighted", "lenient", "tiered")),
            ManualDutyPeriodNoticeCodeLengthRule(80),
            ManualDutyPeriodNoticeChannelLengthRule(80),
            ManualDutyPeriodNoticeUnitLengthRule(120),
        ),
    )
}
