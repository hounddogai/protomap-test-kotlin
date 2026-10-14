package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.text.normalize

/** A rule a manual zone checkpoint must pass. Check returns its problems, or none. */
interface ManualZoneCheckpointRule {
    fun check(item: ManualZoneCheckpoint): MutableList<String>
}

data class ManualZoneCheckpointAllowedCodeRule(var allowed: MutableList<String>) : ManualZoneCheckpointRule {
    override fun check(item: ManualZoneCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ManualZoneCheckpointTitleLengthRule(var maxLength: Int) : ManualZoneCheckpointRule {
    override fun check(item: ManualZoneCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualZoneCheckpointCategoryLengthRule(var maxLength: Int) : ManualZoneCheckpointRule {
    override fun check(item: ManualZoneCheckpoint): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualZoneCheckpointRequiredCodeRule(var message: String) : ManualZoneCheckpointRule {
    override fun check(item: ManualZoneCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual zone checkpoint. */
data class ManualZoneCheckpointValidator(var rules: MutableList<ManualZoneCheckpointRule>) {
    fun validate(item: ManualZoneCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualZoneCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualZoneCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultManualZoneCheckpointValidator(): ManualZoneCheckpointValidator {
    return ManualZoneCheckpointValidator(
        mutableListOf<ManualZoneCheckpointRule>(
            ManualZoneCheckpointAllowedCodeRule(mutableListOf<String>("seasonal")),
            ManualZoneCheckpointTitleLengthRule(32),
            ManualZoneCheckpointCategoryLengthRule(3600),
            ManualZoneCheckpointRequiredCodeRule("manual zone checkpoint needs a value"),
        ),
    )
}
