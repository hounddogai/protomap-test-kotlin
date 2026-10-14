package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import com.acme.transport.kit.text.normalize

/** A rule a scheduled fuel surcharge window must pass. Check returns its problems, or none. */
interface ScheduledFuelSurchargeWindowRule {
    fun check(item: ScheduledFuelSurchargeWindow): MutableList<String>
}

data class ScheduledFuelSurchargeWindowLabelLengthRule(var maxLength: Int) : ScheduledFuelSurchargeWindowRule {
    override fun check(item: ScheduledFuelSurchargeWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeWindowStopCountRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledFuelSurchargeWindowRule {
    override fun check(item: ScheduledFuelSurchargeWindow): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class ScheduledFuelSurchargeWindowAllowedNameRule(
    var allowed: MutableList<String>,
) : ScheduledFuelSurchargeWindowRule {
    override fun check(item: ScheduledFuelSurchargeWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeWindowTimezoneLengthRule(
    var maxLength: Int,
) : ScheduledFuelSurchargeWindowRule {
    override fun check(item: ScheduledFuelSurchargeWindow): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeWindowRequiredTimezoneRule(
    var message: String,
) : ScheduledFuelSurchargeWindowRule {
    override fun check(item: ScheduledFuelSurchargeWindow): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled fuel surcharge window. */
data class ScheduledFuelSurchargeWindowValidator(var rules: MutableList<ScheduledFuelSurchargeWindowRule>) {
    fun validate(item: ScheduledFuelSurchargeWindow): MutableList<String> {
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

    fun requireValid(item: ScheduledFuelSurchargeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFuelSurchargeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFuelSurchargeWindowValidator(): ScheduledFuelSurchargeWindowValidator {
    return ScheduledFuelSurchargeWindowValidator(
        mutableListOf<ScheduledFuelSurchargeWindowRule>(
            ScheduledFuelSurchargeWindowLabelLengthRule(86400),
            ScheduledFuelSurchargeWindowStopCountRangeRule(32, 1000),
            ScheduledFuelSurchargeWindowAllowedNameRule(mutableListOf<String>("weighted", "fallback")),
            ScheduledFuelSurchargeWindowTimezoneLengthRule(32),
            ScheduledFuelSurchargeWindowRequiredTimezoneRule("scheduled fuel surcharge window needs a value"),
        ),
    )
}
