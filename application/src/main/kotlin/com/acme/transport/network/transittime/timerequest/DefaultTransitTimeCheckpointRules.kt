package com.acme.transport.network.transittime.timerequest

/** A rule a default transit time checkpoint must pass. Check returns its problems, or none. */
interface DefaultTransitTimeCheckpointRule {
    fun check(item: DefaultTransitTimeCheckpoint): MutableList<String>
}

data class DefaultTransitTimeCheckpointCodePrefixRule(var prefix: String) : DefaultTransitTimeCheckpointRule {
    override fun check(item: DefaultTransitTimeCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeCheckpointCodeLengthRule(var maxLength: Int) : DefaultTransitTimeCheckpointRule {
    override fun check(item: DefaultTransitTimeCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeCheckpointPinnedGuardRule(var limit: Double) : DefaultTransitTimeCheckpointRule {
    override fun check(item: DefaultTransitTimeCheckpoint): MutableList<String> {
        if (item.pinned && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeCheckpointLocaleLengthRule(var maxLength: Int) : DefaultTransitTimeCheckpointRule {
    override fun check(item: DefaultTransitTimeCheckpoint): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeCheckpointRequiredCodeRule(
    var message: String,
) : DefaultTransitTimeCheckpointRule {
    override fun check(item: DefaultTransitTimeCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default transit time checkpoint. */
data class DefaultTransitTimeCheckpointValidator(var rules: MutableList<DefaultTransitTimeCheckpointRule>) {
    fun validate(item: DefaultTransitTimeCheckpoint): MutableList<String> {
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

    fun requireValid(item: DefaultTransitTimeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTransitTimeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTransitTimeCheckpointValidator(): DefaultTransitTimeCheckpointValidator {
    return DefaultTransitTimeCheckpointValidator(
        mutableListOf<DefaultTransitTimeCheckpointRule>(
            DefaultTransitTimeCheckpointCodePrefixRule("X"),
            DefaultTransitTimeCheckpointCodeLengthRule(16),
            DefaultTransitTimeCheckpointPinnedGuardRule(4.4),
            DefaultTransitTimeCheckpointLocaleLengthRule(80),
            DefaultTransitTimeCheckpointRequiredCodeRule("default transit time checkpoint needs this field"),
        ),
    )
}
