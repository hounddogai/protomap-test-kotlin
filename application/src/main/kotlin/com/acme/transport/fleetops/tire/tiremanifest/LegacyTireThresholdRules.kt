package com.acme.transport.fleetops.tire.tiremanifest

/** A rule a legacy tire threshold must pass. Check returns its problems, or none. */
interface LegacyTireThresholdRule {
    fun check(item: LegacyTireThreshold): MutableList<String>
}

data class LegacyTireThresholdLabelLengthRule(var maxLength: Int) : LegacyTireThresholdRule {
    override fun check(item: LegacyTireThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireThresholdCodePrefixRule(var prefix: String) : LegacyTireThresholdRule {
    override fun check(item: LegacyTireThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireThresholdRequiredLabelRule(var message: String) : LegacyTireThresholdRule {
    override fun check(item: LegacyTireThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tire threshold. */
data class LegacyTireThresholdValidator(var rules: MutableList<LegacyTireThresholdRule>) {
    fun validate(item: LegacyTireThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTireThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTireThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTireThresholdValidator(): LegacyTireThresholdValidator {
    return LegacyTireThresholdValidator(
        mutableListOf<LegacyTireThresholdRule>(
            LegacyTireThresholdLabelLengthRule(80),
            LegacyTireThresholdCodePrefixRule("OPS"),
            LegacyTireThresholdRequiredLabelRule("legacy tire threshold needs a value"),
        ),
    )
}
