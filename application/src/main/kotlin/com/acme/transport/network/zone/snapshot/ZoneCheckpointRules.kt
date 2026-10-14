package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a zone checkpoint must pass. Check returns its problems, or none. */
interface ZoneCheckpointRule {
    fun check(item: ZoneCheckpoint): MutableList<String>
}

data class ZoneCheckpointCodePrefixRule(var prefix: String) : ZoneCheckpointRule {
    override fun check(item: ZoneCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneCheckpointLabelLengthRule(var maxLength: Int) : ZoneCheckpointRule {
    override fun check(item: ZoneCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneCheckpointMarginRangeRule(var min: Double, var max: Double) : ZoneCheckpointRule {
    override fun check(item: ZoneCheckpoint): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class ZoneCheckpointAllowedLabelRule(var allowed: MutableList<String>) : ZoneCheckpointRule {
    override fun check(item: ZoneCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone checkpoint. */
data class ZoneCheckpointValidator(var rules: MutableList<ZoneCheckpointRule>) {
    fun validate(item: ZoneCheckpoint): MutableList<String> {
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

    fun requireValid(item: ZoneCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneCheckpointValidator(): ZoneCheckpointValidator {
    return ZoneCheckpointValidator(
        mutableListOf<ZoneCheckpointRule>(
            ZoneCheckpointCodePrefixRule("ACM"),
            ZoneCheckpointLabelLengthRule(1000),
            ZoneCheckpointMarginRangeRule(2.6, 4496.1),
            ZoneCheckpointAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
