package com.acme.transport.network.zone.quota

import com.acme.transport.kit.text.normalize

/** A rule a zone window must pass. Check returns its problems, or none. */
interface ZoneWindowRule {
    fun check(item: ZoneWindow): MutableList<String>
}

data class ZoneWindowCreatedAtRangeRule(var min: Int, var max: Int) : ZoneWindowRule {
    override fun check(item: ZoneWindow): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneWindowStopCountRangeRule(var min: Int, var max: Int) : ZoneWindowRule {
    override fun check(item: ZoneWindow): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneWindowRequiredDescriptionRule(var message: String) : ZoneWindowRule {
    override fun check(item: ZoneWindow): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneWindowTitleLengthRule(var maxLength: Int) : ZoneWindowRule {
    override fun check(item: ZoneWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneWindowAllowedDescriptionRule(var allowed: MutableList<String>) : ZoneWindowRule {
    override fun check(item: ZoneWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ZoneWindowUpdatedAtRangeRule(var min: Int, var max: Int) : ZoneWindowRule {
    override fun check(item: ZoneWindow): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone window. */
data class ZoneWindowValidator(var rules: MutableList<ZoneWindowRule>) {
    fun validate(item: ZoneWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneWindowValidator(): ZoneWindowValidator {
    return ZoneWindowValidator(
        mutableListOf<ZoneWindowRule>(
            ZoneWindowCreatedAtRangeRule(1000, 3600),
            ZoneWindowStopCountRangeRule(120, 120),
            ZoneWindowRequiredDescriptionRule("zone window needs an entry"),
            ZoneWindowTitleLengthRule(80),
            ZoneWindowAllowedDescriptionRule(mutableListOf<String>("default")),
            ZoneWindowUpdatedAtRangeRule(120, 1000),
        ),
    )
}
