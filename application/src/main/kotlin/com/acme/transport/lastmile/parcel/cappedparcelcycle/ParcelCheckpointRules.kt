package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.text.normalize

/** A rule a parcel checkpoint must pass. Check returns its problems, or none. */
interface ParcelCheckpointRule {
    fun check(item: ParcelCheckpoint): MutableList<String>
}

data class ParcelCheckpointRequiredCodeRule(var message: String) : ParcelCheckpointRule {
    override fun check(item: ParcelCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelCheckpointNameLengthRule(var maxLength: Int) : ParcelCheckpointRule {
    override fun check(item: ParcelCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelCheckpointAllowedDockRule(var allowed: MutableList<String>) : ParcelCheckpointRule {
    override fun check(item: ParcelCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ParcelCheckpointRequiredNameRule(var message: String) : ParcelCheckpointRule {
    override fun check(item: ParcelCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelCheckpointCeilingRangeRule(var min: Double, var max: Double) : ParcelCheckpointRule {
    override fun check(item: ParcelCheckpoint): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelCheckpointDepotLengthRule(var maxLength: Int) : ParcelCheckpointRule {
    override fun check(item: ParcelCheckpoint): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel checkpoint. */
data class ParcelCheckpointValidator(var rules: MutableList<ParcelCheckpointRule>) {
    fun validate(item: ParcelCheckpoint): MutableList<String> {
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

    fun requireValid(item: ParcelCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelCheckpointValidator(): ParcelCheckpointValidator {
    return ParcelCheckpointValidator(
        mutableListOf<ParcelCheckpointRule>(
            ParcelCheckpointRequiredCodeRule("parcel checkpoint needs a value"),
            ParcelCheckpointNameLengthRule(16),
            ParcelCheckpointAllowedDockRule(mutableListOf<String>()),
            ParcelCheckpointRequiredNameRule("parcel checkpoint needs a value"),
            ParcelCheckpointCeilingRangeRule(3.5, 969.1),
            ParcelCheckpointDepotLengthRule(32),
        ),
    )
}
