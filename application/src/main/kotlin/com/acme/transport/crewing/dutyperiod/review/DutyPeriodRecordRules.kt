package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.text.normalize

/** A rule a duty period record must pass. Check returns its problems, or none. */
interface DutyPeriodRecordRule {
    fun check(item: DutyPeriodRecord): MutableList<String>
}

data class DutyPeriodRecordRequiredNameRule(var message: String) : DutyPeriodRecordRule {
    override fun check(item: DutyPeriodRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodRecordTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DutyPeriodRecordRule {
    override fun check(item: DutyPeriodRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class DutyPeriodRecordAllowedCodeRule(var allowed: MutableList<String>) : DutyPeriodRecordRule {
    override fun check(item: DutyPeriodRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period record. */
data class DutyPeriodRecordValidator(var rules: MutableList<DutyPeriodRecordRule>) {
    fun validate(item: DutyPeriodRecord): MutableList<String> {
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

    fun requireValid(item: DutyPeriodRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodRecordValidator(): DutyPeriodRecordValidator {
    return DutyPeriodRecordValidator(
        mutableListOf<DutyPeriodRecordRule>(
            DutyPeriodRecordRequiredNameRule("duty period record needs an entry"),
            DutyPeriodRecordTagsLimitRule(3600, mutableListOf<String>()),
            DutyPeriodRecordAllowedCodeRule(mutableListOf<String>("pending", "strict", "regional")),
        ),
    )
}
