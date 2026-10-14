package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.normalize

/** A rule a pending fuel burn threshold must pass. Check returns its problems, or none. */
interface PendingFuelBurnThresholdRule {
    fun check(item: PendingFuelBurnThreshold): MutableList<String>
}

data class PendingFuelBurnThresholdCodePrefixRule(var prefix: String) : PendingFuelBurnThresholdRule {
    override fun check(item: PendingFuelBurnThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnThresholdRegionLengthRule(var maxLength: Int) : PendingFuelBurnThresholdRule {
    override fun check(item: PendingFuelBurnThreshold): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnThresholdAllowedNotesRule(
    var allowed: MutableList<String>,
) : PendingFuelBurnThresholdRule {
    override fun check(item: PendingFuelBurnThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PendingFuelBurnThresholdRequiredLabelRule(var message: String) : PendingFuelBurnThresholdRule {
    override fun check(item: PendingFuelBurnThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending fuel burn threshold. */
data class PendingFuelBurnThresholdValidator(var rules: MutableList<PendingFuelBurnThresholdRule>) {
    fun validate(item: PendingFuelBurnThreshold): MutableList<String> {
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

    fun requireValid(item: PendingFuelBurnThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFuelBurnThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFuelBurnThresholdValidator(): PendingFuelBurnThresholdValidator {
    return PendingFuelBurnThresholdValidator(
        mutableListOf<PendingFuelBurnThresholdRule>(
            PendingFuelBurnThresholdCodePrefixRule("REF"),
            PendingFuelBurnThresholdRegionLengthRule(3600),
            PendingFuelBurnThresholdAllowedNotesRule(mutableListOf<String>()),
            PendingFuelBurnThresholdRequiredLabelRule("pending fuel burn threshold needs a value"),
        ),
    )
}
