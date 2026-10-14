package com.acme.transport.network.node.profile

/** A rule a regional node checkpoint must pass. Check returns its problems, or none. */
interface RegionalNodeCheckpointRule {
    fun check(item: RegionalNodeCheckpoint): MutableList<String>
}

data class RegionalNodeCheckpointTrailerIdLengthRule(var maxLength: Int) : RegionalNodeCheckpointRule {
    override fun check(item: RegionalNodeCheckpoint): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeCheckpointDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalNodeCheckpointRule {
    override fun check(item: RegionalNodeCheckpoint): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeCheckpointRequiredCodeRule(var message: String) : RegionalNodeCheckpointRule {
    override fun check(item: RegionalNodeCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeCheckpointCodePrefixRule(var prefix: String) : RegionalNodeCheckpointRule {
    override fun check(item: RegionalNodeCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeCheckpointRequiredTitleRule(var message: String) : RegionalNodeCheckpointRule {
    override fun check(item: RegionalNodeCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional node checkpoint. */
data class RegionalNodeCheckpointValidator(var rules: MutableList<RegionalNodeCheckpointRule>) {
    fun validate(item: RegionalNodeCheckpoint): MutableList<String> {
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

    fun requireValid(item: RegionalNodeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalNodeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalNodeCheckpointValidator(): RegionalNodeCheckpointValidator {
    return RegionalNodeCheckpointValidator(
        mutableListOf<RegionalNodeCheckpointRule>(
            RegionalNodeCheckpointTrailerIdLengthRule(16),
            RegionalNodeCheckpointDurationMinutesRangeRule(255, 255),
            RegionalNodeCheckpointRequiredCodeRule("regional node checkpoint needs an entry"),
            RegionalNodeCheckpointCodePrefixRule("Q"),
            RegionalNodeCheckpointRequiredTitleRule("regional node checkpoint needs a value"),
        ),
    )
}
