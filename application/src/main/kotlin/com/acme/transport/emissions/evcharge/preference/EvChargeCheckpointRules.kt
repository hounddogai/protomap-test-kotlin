package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.text.normalize

/** A rule a ev charge checkpoint must pass. Check returns its problems, or none. */
interface EvChargeCheckpointRule {
    fun check(item: EvChargeCheckpoint): MutableList<String>
}

data class EvChargeCheckpointAllowedLabelRule(var allowed: MutableList<String>) : EvChargeCheckpointRule {
    override fun check(item: EvChargeCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeCheckpointPalletsRangeRule(var min: Int, var max: Int) : EvChargeCheckpointRule {
    override fun check(item: EvChargeCheckpoint): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeCheckpointTitleLengthRule(var maxLength: Int) : EvChargeCheckpointRule {
    override fun check(item: EvChargeCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeCheckpointCodeLengthRule(var maxLength: Int) : EvChargeCheckpointRule {
    override fun check(item: EvChargeCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge checkpoint. */
data class EvChargeCheckpointValidator(var rules: MutableList<EvChargeCheckpointRule>) {
    fun validate(item: EvChargeCheckpoint): MutableList<String> {
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

    fun requireValid(item: EvChargeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeCheckpointValidator(): EvChargeCheckpointValidator {
    return EvChargeCheckpointValidator(
        mutableListOf<EvChargeCheckpointRule>(
            EvChargeCheckpointAllowedLabelRule(mutableListOf<String>("primary", "global")),
            EvChargeCheckpointPalletsRangeRule(120, 3600),
            EvChargeCheckpointTitleLengthRule(1000),
            EvChargeCheckpointCodeLengthRule(1000),
        ),
    )
}
