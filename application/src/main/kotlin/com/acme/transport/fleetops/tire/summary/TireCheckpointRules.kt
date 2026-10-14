package com.acme.transport.fleetops.tire.summary

import com.acme.transport.kit.text.normalize

/** A rule a tire checkpoint must pass. Check returns its problems, or none. */
interface TireCheckpointRule {
    fun check(item: TireCheckpoint): MutableList<String>
}

data class TireCheckpointLabelLengthRule(var maxLength: Int) : TireCheckpointRule {
    override fun check(item: TireCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireCheckpointRequiredTimezoneRule(var message: String) : TireCheckpointRule {
    override fun check(item: TireCheckpoint): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireCheckpointAllowedCarrierRule(var allowed: MutableList<String>) : TireCheckpointRule {
    override fun check(item: TireCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TireCheckpointLoadKgRangeRule(var min: Double, var max: Double) : TireCheckpointRule {
    override fun check(item: TireCheckpoint): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireCheckpointCarrierLengthRule(var maxLength: Int) : TireCheckpointRule {
    override fun check(item: TireCheckpoint): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireCheckpointRequiredLabelRule(var message: String) : TireCheckpointRule {
    override fun check(item: TireCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire checkpoint. */
data class TireCheckpointValidator(var rules: MutableList<TireCheckpointRule>) {
    fun validate(item: TireCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTireCheckpointValidator(): TireCheckpointValidator {
    return TireCheckpointValidator(
        mutableListOf<TireCheckpointRule>(
            TireCheckpointLabelLengthRule(32),
            TireCheckpointRequiredTimezoneRule("tire checkpoint needs an entry"),
            TireCheckpointAllowedCarrierRule(mutableListOf<String>("global")),
            TireCheckpointLoadKgRangeRule(1.2, 4035.7),
            TireCheckpointCarrierLengthRule(3600),
            TireCheckpointRequiredLabelRule("tire checkpoint needs a value"),
        ),
    )
}
