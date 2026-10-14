package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.text.normalize

/** A rule a scheduled trailer window must pass. Check returns its problems, or none. */
interface ScheduledTrailerWindowRule {
    fun check(item: ScheduledTrailerWindow): MutableList<String>
}

data class ScheduledTrailerWindowTitleLengthRule(var maxLength: Int) : ScheduledTrailerWindowRule {
    override fun check(item: ScheduledTrailerWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerWindowCodePrefixRule(var prefix: String) : ScheduledTrailerWindowRule {
    override fun check(item: ScheduledTrailerWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerWindowAllowedTitleRule(
    var allowed: MutableList<String>,
) : ScheduledTrailerWindowRule {
    override fun check(item: ScheduledTrailerWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerWindowOdometerRangeRule(var min: Int, var max: Int) : ScheduledTrailerWindowRule {
    override fun check(item: ScheduledTrailerWindow): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerWindowRequiredRegionRule(var message: String) : ScheduledTrailerWindowRule {
    override fun check(item: ScheduledTrailerWindow): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerWindowRequiredCodeRule(var message: String) : ScheduledTrailerWindowRule {
    override fun check(item: ScheduledTrailerWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled trailer window. */
data class ScheduledTrailerWindowValidator(var rules: MutableList<ScheduledTrailerWindowRule>) {
    fun validate(item: ScheduledTrailerWindow): MutableList<String> {
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

    fun requireValid(item: ScheduledTrailerWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTrailerWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTrailerWindowValidator(): ScheduledTrailerWindowValidator {
    return ScheduledTrailerWindowValidator(
        mutableListOf<ScheduledTrailerWindowRule>(
            ScheduledTrailerWindowTitleLengthRule(32),
            ScheduledTrailerWindowCodePrefixRule("OPS"),
            ScheduledTrailerWindowAllowedTitleRule(mutableListOf<String>("manual")),
            ScheduledTrailerWindowOdometerRangeRule(16, 86400),
            ScheduledTrailerWindowRequiredRegionRule("scheduled trailer window needs this field"),
            ScheduledTrailerWindowRequiredCodeRule("scheduled trailer window needs a value"),
        ),
    )
}
