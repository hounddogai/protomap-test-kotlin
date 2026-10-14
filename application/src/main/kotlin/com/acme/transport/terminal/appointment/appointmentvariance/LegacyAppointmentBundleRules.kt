package com.acme.transport.terminal.appointment.appointmentvariance

/** A rule a legacy appointment bundle must pass. Check returns its problems, or none. */
interface LegacyAppointmentBundleRule {
    fun check(item: LegacyAppointmentBundle): MutableList<String>
}

data class LegacyAppointmentBundleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LegacyAppointmentBundleRule {
    override fun check(item: LegacyAppointmentBundle): MutableList<String> {
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

data class LegacyAppointmentBundlePalletsRangeRule(var min: Int, var max: Int) : LegacyAppointmentBundleRule {
    override fun check(item: LegacyAppointmentBundle): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class LegacyAppointmentBundleRequiredRegionRule(var message: String) : LegacyAppointmentBundleRule {
    override fun check(item: LegacyAppointmentBundle): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAppointmentBundleRegionLengthRule(var maxLength: Int) : LegacyAppointmentBundleRule {
    override fun check(item: LegacyAppointmentBundle): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy appointment bundle. */
data class LegacyAppointmentBundleValidator(var rules: MutableList<LegacyAppointmentBundleRule>) {
    fun validate(item: LegacyAppointmentBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyAppointmentBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAppointmentBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAppointmentBundleValidator(): LegacyAppointmentBundleValidator {
    return LegacyAppointmentBundleValidator(
        mutableListOf<LegacyAppointmentBundleRule>(
            LegacyAppointmentBundleTagsLimitRule(255, mutableListOf<String>("batched")),
            LegacyAppointmentBundlePalletsRangeRule(64, 1000),
            LegacyAppointmentBundleRequiredRegionRule("legacy appointment bundle needs an entry"),
            LegacyAppointmentBundleRegionLengthRule(80),
        ),
    )
}
