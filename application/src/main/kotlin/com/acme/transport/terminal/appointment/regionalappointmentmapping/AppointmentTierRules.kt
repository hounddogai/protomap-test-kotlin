package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.text.normalize

/** A rule a appointment tier must pass. Check returns its problems, or none. */
interface AppointmentTierRule {
    fun check(item: AppointmentTier): MutableList<String>
}

data class AppointmentTierTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : AppointmentTierRule {
    override fun check(item: AppointmentTier): MutableList<String> {
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

data class AppointmentTierAllowedLabelRule(var allowed: MutableList<String>) : AppointmentTierRule {
    override fun check(item: AppointmentTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentTierCodeLengthRule(var maxLength: Int) : AppointmentTierRule {
    override fun check(item: AppointmentTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentTierDepotLengthRule(var maxLength: Int) : AppointmentTierRule {
    override fun check(item: AppointmentTier): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment tier. */
data class AppointmentTierValidator(var rules: MutableList<AppointmentTierRule>) {
    fun validate(item: AppointmentTier): MutableList<String> {
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

    fun requireValid(item: AppointmentTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentTier>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentTierValidator(): AppointmentTierValidator {
    return AppointmentTierValidator(
        mutableListOf<AppointmentTierRule>(
            AppointmentTierTagsLimitRule(32, mutableListOf<String>("manual", "default")),
            AppointmentTierAllowedLabelRule(mutableListOf<String>()),
            AppointmentTierCodeLengthRule(16),
            AppointmentTierDepotLengthRule(255),
        ),
    )
}
