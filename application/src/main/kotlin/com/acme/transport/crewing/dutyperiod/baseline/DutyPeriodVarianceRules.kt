package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.normalize

/** A rule a duty period variance must pass. Check returns its problems, or none. */
interface DutyPeriodVarianceRule {
    fun check(item: DutyPeriodVariance): MutableList<String>
}

data class DutyPeriodVarianceAllowedNameRule(var allowed: MutableList<String>) : DutyPeriodVarianceRule {
    override fun check(item: DutyPeriodVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodVarianceCodePrefixRule(var prefix: String) : DutyPeriodVarianceRule {
    override fun check(item: DutyPeriodVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodVarianceRequiredCodeRule(var message: String) : DutyPeriodVarianceRule {
    override fun check(item: DutyPeriodVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodVarianceNameLengthRule(var maxLength: Int) : DutyPeriodVarianceRule {
    override fun check(item: DutyPeriodVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodVarianceTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DutyPeriodVarianceRule {
    override fun check(item: DutyPeriodVariance): MutableList<String> {
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

data class DutyPeriodVarianceRequiredNameRule(var message: String) : DutyPeriodVarianceRule {
    override fun check(item: DutyPeriodVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period variance. */
data class DutyPeriodVarianceValidator(var rules: MutableList<DutyPeriodVarianceRule>) {
    fun validate(item: DutyPeriodVariance): MutableList<String> {
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

    fun requireValid(item: DutyPeriodVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodVarianceValidator(): DutyPeriodVarianceValidator {
    return DutyPeriodVarianceValidator(
        mutableListOf<DutyPeriodVarianceRule>(
            DutyPeriodVarianceAllowedNameRule(mutableListOf<String>("tiered")),
            DutyPeriodVarianceCodePrefixRule("REF"),
            DutyPeriodVarianceRequiredCodeRule("duty period variance needs a value"),
            DutyPeriodVarianceNameLengthRule(16),
            DutyPeriodVarianceTagsLimitRule(32, mutableListOf<String>()),
            DutyPeriodVarianceRequiredNameRule("duty period variance needs this field"),
        ),
    )
}
