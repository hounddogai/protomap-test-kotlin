package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.text.normalize

/** A rule a manual camera event summary must pass. Check returns its problems, or none. */
interface ManualCameraEventSummaryRule {
    fun check(item: ManualCameraEventSummary): MutableList<String>
}

data class ManualCameraEventSummaryRequiredDescriptionRule(var message: String) : ManualCameraEventSummaryRule {
    override fun check(item: ManualCameraEventSummary): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventSummaryRequiredCodeRule(var message: String) : ManualCameraEventSummaryRule {
    override fun check(item: ManualCameraEventSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventSummaryOdometerRangeRule(
    var min: Int,
    var max: Int,
) : ManualCameraEventSummaryRule {
    override fun check(item: ManualCameraEventSummary): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class ManualCameraEventSummaryAllowedNameRule(
    var allowed: MutableList<String>,
) : ManualCameraEventSummaryRule {
    override fun check(item: ManualCameraEventSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventSummaryCodePrefixRule(var prefix: String) : ManualCameraEventSummaryRule {
    override fun check(item: ManualCameraEventSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventSummaryRequiredNameRule(var message: String) : ManualCameraEventSummaryRule {
    override fun check(item: ManualCameraEventSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual camera event summary. */
data class ManualCameraEventSummaryValidator(var rules: MutableList<ManualCameraEventSummaryRule>) {
    fun validate(item: ManualCameraEventSummary): MutableList<String> {
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

    fun requireValid(item: ManualCameraEventSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCameraEventSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCameraEventSummaryValidator(): ManualCameraEventSummaryValidator {
    return ManualCameraEventSummaryValidator(
        mutableListOf<ManualCameraEventSummaryRule>(
            ManualCameraEventSummaryRequiredDescriptionRule("manual camera event summary needs this field"),
            ManualCameraEventSummaryRequiredCodeRule("manual camera event summary needs a value"),
            ManualCameraEventSummaryOdometerRangeRule(16, 86400),
            ManualCameraEventSummaryAllowedNameRule(mutableListOf<String>("default", "lenient")),
            ManualCameraEventSummaryCodePrefixRule("OPS"),
            ManualCameraEventSummaryRequiredNameRule("manual camera event summary needs this field"),
        ),
    )
}
