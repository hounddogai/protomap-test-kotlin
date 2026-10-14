package com.acme.transport.crewing.restbreak.claim

/** A rule a fallback rest break checkpoint must pass. Check returns its problems, or none. */
interface FallbackRestBreakCheckpointRule {
    fun check(item: FallbackRestBreakCheckpoint): MutableList<String>
}

data class FallbackRestBreakCheckpointRequiredTrailerIdRule(
    var message: String,
) : FallbackRestBreakCheckpointRule {
    override fun check(item: FallbackRestBreakCheckpoint): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakCheckpointSequenceRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRestBreakCheckpointRule {
    override fun check(item: FallbackRestBreakCheckpoint): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class FallbackRestBreakCheckpointArchivedGuardRule(var limit: Int) : FallbackRestBreakCheckpointRule {
    override fun check(item: FallbackRestBreakCheckpoint): MutableList<String> {
        if (item.archived && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakCheckpointTrailerIdLengthRule(
    var maxLength: Int,
) : FallbackRestBreakCheckpointRule {
    override fun check(item: FallbackRestBreakCheckpoint): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback rest break checkpoint. */
data class FallbackRestBreakCheckpointValidator(var rules: MutableList<FallbackRestBreakCheckpointRule>) {
    fun validate(item: FallbackRestBreakCheckpoint): MutableList<String> {
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

    fun requireValid(item: FallbackRestBreakCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRestBreakCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRestBreakCheckpointValidator(): FallbackRestBreakCheckpointValidator {
    return FallbackRestBreakCheckpointValidator(
        mutableListOf<FallbackRestBreakCheckpointRule>(
            FallbackRestBreakCheckpointRequiredTrailerIdRule("fallback rest break checkpoint needs a value"),
            FallbackRestBreakCheckpointSequenceRangeRule(32, 1000),
            FallbackRestBreakCheckpointArchivedGuardRule(16),
            FallbackRestBreakCheckpointTrailerIdLengthRule(80),
        ),
    )
}
