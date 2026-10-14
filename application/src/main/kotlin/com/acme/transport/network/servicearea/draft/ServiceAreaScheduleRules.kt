package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.normalize

/** A rule a service area schedule must pass. Check returns its problems, or none. */
interface ServiceAreaScheduleRule {
    fun check(item: ServiceAreaSchedule): MutableList<String>
}

data class ServiceAreaScheduleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ServiceAreaScheduleRule {
    override fun check(item: ServiceAreaSchedule): MutableList<String> {
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

data class ServiceAreaScheduleCurrencyLengthRule(var maxLength: Int) : ServiceAreaScheduleRule {
    override fun check(item: ServiceAreaSchedule): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaScheduleRequiredCurrencyRule(var message: String) : ServiceAreaScheduleRule {
    override fun check(item: ServiceAreaSchedule): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaScheduleCodeLengthRule(var maxLength: Int) : ServiceAreaScheduleRule {
    override fun check(item: ServiceAreaSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaScheduleAllowedTitleRule(var allowed: MutableList<String>) : ServiceAreaScheduleRule {
    override fun check(item: ServiceAreaSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area schedule. */
data class ServiceAreaScheduleValidator(var rules: MutableList<ServiceAreaScheduleRule>) {
    fun validate(item: ServiceAreaSchedule): MutableList<String> {
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

    fun requireValid(item: ServiceAreaSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaScheduleValidator(): ServiceAreaScheduleValidator {
    return ServiceAreaScheduleValidator(
        mutableListOf<ServiceAreaScheduleRule>(
            ServiceAreaScheduleTagsLimitRule(255, mutableListOf<String>("fallback", "lenient")),
            ServiceAreaScheduleCurrencyLengthRule(120),
            ServiceAreaScheduleRequiredCurrencyRule("service area schedule needs a value"),
            ServiceAreaScheduleCodeLengthRule(86400),
            ServiceAreaScheduleAllowedTitleRule(mutableListOf<String>("tiered")),
        ),
    )
}
