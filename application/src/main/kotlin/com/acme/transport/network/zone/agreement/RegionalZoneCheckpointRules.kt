package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.text.normalize

/** A rule a regional zone checkpoint must pass. Check returns its problems, or none. */
interface RegionalZoneCheckpointRule {
    fun check(item: RegionalZoneCheckpoint): MutableList<String>
}

data class RegionalZoneCheckpointRequiredCodeRule(var message: String) : RegionalZoneCheckpointRule {
    override fun check(item: RegionalZoneCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneCheckpointRequiredTitleRule(var message: String) : RegionalZoneCheckpointRule {
    override fun check(item: RegionalZoneCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneCheckpointAllowedTitleRule(
    var allowed: MutableList<String>,
) : RegionalZoneCheckpointRule {
    override fun check(item: RegionalZoneCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneCheckpointThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RegionalZoneCheckpointRule {
    override fun check(item: RegionalZoneCheckpoint): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class RegionalZoneCheckpointCodeLengthRule(var maxLength: Int) : RegionalZoneCheckpointRule {
    override fun check(item: RegionalZoneCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneCheckpointMinimumRangeRule(
    var min: Double,
    var max: Double,
) : RegionalZoneCheckpointRule {
    override fun check(item: RegionalZoneCheckpoint): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional zone checkpoint. */
data class RegionalZoneCheckpointValidator(var rules: MutableList<RegionalZoneCheckpointRule>) {
    fun validate(item: RegionalZoneCheckpoint): MutableList<String> {
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

    fun requireValid(item: RegionalZoneCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalZoneCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalZoneCheckpointValidator(): RegionalZoneCheckpointValidator {
    return RegionalZoneCheckpointValidator(
        mutableListOf<RegionalZoneCheckpointRule>(
            RegionalZoneCheckpointRequiredCodeRule("regional zone checkpoint needs this field"),
            RegionalZoneCheckpointRequiredTitleRule("regional zone checkpoint needs a value"),
            RegionalZoneCheckpointAllowedTitleRule(mutableListOf<String>("capped", "scheduled")),
            RegionalZoneCheckpointThresholdRangeRule(5.7, 4232.3),
            RegionalZoneCheckpointCodeLengthRule(255),
            RegionalZoneCheckpointMinimumRangeRule(2.2, 2961.7),
        ),
    )
}
