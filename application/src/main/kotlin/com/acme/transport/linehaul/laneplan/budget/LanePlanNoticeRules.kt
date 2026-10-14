package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.text.normalize

/** A rule a lane plan notice must pass. Check returns its problems, or none. */
interface LanePlanNoticeRule {
    fun check(item: LanePlanNotice): MutableList<String>
}

data class LanePlanNoticeTitleLengthRule(var maxLength: Int) : LanePlanNoticeRule {
    override fun check(item: LanePlanNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanNoticeApprovedGuardRule(var limit: Int) : LanePlanNoticeRule {
    override fun check(item: LanePlanNotice): MutableList<String> {
        if (item.approved && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanNoticeEtaMinutesRangeRule(var min: Int, var max: Int) : LanePlanNoticeRule {
    override fun check(item: LanePlanNotice): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanNoticeNameLengthRule(var maxLength: Int) : LanePlanNoticeRule {
    override fun check(item: LanePlanNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanNoticeAllowedTitleRule(var allowed: MutableList<String>) : LanePlanNoticeRule {
    override fun check(item: LanePlanNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan notice. */
data class LanePlanNoticeValidator(var rules: MutableList<LanePlanNoticeRule>) {
    fun validate(item: LanePlanNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanNoticeValidator(): LanePlanNoticeValidator {
    return LanePlanNoticeValidator(
        mutableListOf<LanePlanNoticeRule>(
            LanePlanNoticeTitleLengthRule(16),
            LanePlanNoticeApprovedGuardRule(1000),
            LanePlanNoticeEtaMinutesRangeRule(1000, 86400),
            LanePlanNoticeNameLengthRule(1000),
            LanePlanNoticeAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
