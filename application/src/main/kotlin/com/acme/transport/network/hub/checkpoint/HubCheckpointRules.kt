package com.acme.transport.network.hub.checkpoint

/** A rule a hub checkpoint must pass. Check returns its problems, or none. */
interface HubCheckpointRule {
    fun check(item: HubCheckpoint): MutableList<String>
}

data class HubCheckpointRequiredCodeRule(var message: String) : HubCheckpointRule {
    override fun check(item: HubCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubCheckpointNotesLengthRule(var maxLength: Int) : HubCheckpointRule {
    override fun check(item: HubCheckpoint): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubCheckpointRequiredNotesRule(var message: String) : HubCheckpointRule {
    override fun check(item: HubCheckpoint): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubCheckpointTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : HubCheckpointRule {
    override fun check(item: HubCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class HubCheckpointCodePrefixRule(var prefix: String) : HubCheckpointRule {
    override fun check(item: HubCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubCheckpointMinimumRangeRule(var min: Double, var max: Double) : HubCheckpointRule {
    override fun check(item: HubCheckpoint): MutableList<String> {
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

/** Runs every rule of a hub checkpoint. */
data class HubCheckpointValidator(var rules: MutableList<HubCheckpointRule>) {
    fun validate(item: HubCheckpoint): MutableList<String> {
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

    fun requireValid(item: HubCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultHubCheckpointValidator(): HubCheckpointValidator {
    return HubCheckpointValidator(
        mutableListOf<HubCheckpointRule>(
            HubCheckpointRequiredCodeRule("hub checkpoint needs a value"),
            HubCheckpointNotesLengthRule(86400),
            HubCheckpointRequiredNotesRule("hub checkpoint needs a value"),
            HubCheckpointTagsLimitRule(80, mutableListOf<String>("manual")),
            HubCheckpointCodePrefixRule("Q"),
            HubCheckpointMinimumRangeRule(0.7, 2416.9),
        ),
    )
}
