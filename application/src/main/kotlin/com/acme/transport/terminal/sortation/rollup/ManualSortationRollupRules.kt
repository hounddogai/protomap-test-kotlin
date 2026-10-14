package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.text.normalize

/** A rule a manual sortation rollup must pass. Check returns its problems, or none. */
interface ManualSortationRollupRule {
    fun check(item: ManualSortationRollup): MutableList<String>
}

data class ManualSortationRollupRequiredDepotRule(var message: String) : ManualSortationRollupRule {
    override fun check(item: ManualSortationRollup): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualSortationRollupLaneLengthRule(var maxLength: Int) : ManualSortationRollupRule {
    override fun check(item: ManualSortationRollup): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualSortationRollupCodePrefixRule(var prefix: String) : ManualSortationRollupRule {
    override fun check(item: ManualSortationRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualSortationRollupAllowedNameRule(var allowed: MutableList<String>) : ManualSortationRollupRule {
    override fun check(item: ManualSortationRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ManualSortationRollupOdometerRangeRule(var min: Int, var max: Int) : ManualSortationRollupRule {
    override fun check(item: ManualSortationRollup): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualSortationRollupRequiredLaneRule(var message: String) : ManualSortationRollupRule {
    override fun check(item: ManualSortationRollup): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual sortation rollup. */
data class ManualSortationRollupValidator(var rules: MutableList<ManualSortationRollupRule>) {
    fun validate(item: ManualSortationRollup): MutableList<String> {
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

    fun requireValid(item: ManualSortationRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualSortationRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultManualSortationRollupValidator(): ManualSortationRollupValidator {
    return ManualSortationRollupValidator(
        mutableListOf<ManualSortationRollupRule>(
            ManualSortationRollupRequiredDepotRule("manual sortation rollup needs an entry"),
            ManualSortationRollupLaneLengthRule(1000),
            ManualSortationRollupCodePrefixRule("X"),
            ManualSortationRollupAllowedNameRule(mutableListOf<String>("scheduled", "seasonal", "regional")),
            ManualSortationRollupOdometerRangeRule(1000, 3600),
            ManualSortationRollupRequiredLaneRule("manual sortation rollup needs an entry"),
        ),
    )
}
