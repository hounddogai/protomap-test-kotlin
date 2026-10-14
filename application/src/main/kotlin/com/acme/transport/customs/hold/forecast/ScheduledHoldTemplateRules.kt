package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.text.normalize

/** A rule a scheduled hold template must pass. Check returns its problems, or none. */
interface ScheduledHoldTemplateRule {
    fun check(item: ScheduledHoldTemplate): MutableList<String>
}

data class ScheduledHoldTemplateRequiredTitleRule(var message: String) : ScheduledHoldTemplateRule {
    override fun check(item: ScheduledHoldTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldTemplateRequiredVehicleIdRule(var message: String) : ScheduledHoldTemplateRule {
    override fun check(item: ScheduledHoldTemplate): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldTemplateAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : ScheduledHoldTemplateRule {
    override fun check(item: ScheduledHoldTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldTemplateVehicleIdLengthRule(var maxLength: Int) : ScheduledHoldTemplateRule {
    override fun check(item: ScheduledHoldTemplate): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hold template. */
data class ScheduledHoldTemplateValidator(var rules: MutableList<ScheduledHoldTemplateRule>) {
    fun validate(item: ScheduledHoldTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledHoldTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHoldTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHoldTemplateValidator(): ScheduledHoldTemplateValidator {
    return ScheduledHoldTemplateValidator(
        mutableListOf<ScheduledHoldTemplateRule>(
            ScheduledHoldTemplateRequiredTitleRule("scheduled hold template needs a value"),
            ScheduledHoldTemplateRequiredVehicleIdRule("scheduled hold template needs this field"),
            ScheduledHoldTemplateAllowedVehicleIdRule(mutableListOf<String>("pending", "strict", "regional")),
            ScheduledHoldTemplateVehicleIdLengthRule(255),
        ),
    )
}
