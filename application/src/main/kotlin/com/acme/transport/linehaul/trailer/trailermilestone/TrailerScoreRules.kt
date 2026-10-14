package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.normalize

/** A rule a trailer score must pass. Check returns its problems, or none. */
interface TrailerScoreRule {
    fun check(item: TrailerScore): MutableList<String>
}

data class TrailerScoreRequiredUnitRule(var message: String) : TrailerScoreRule {
    override fun check(item: TrailerScore): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerScoreArchivedGuardRule(var limit: Int) : TrailerScoreRule {
    override fun check(item: TrailerScore): MutableList<String> {
        if (item.archived && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class TrailerScoreAllowedReviewerRule(var allowed: MutableList<String>) : TrailerScoreRule {
    override fun check(item: TrailerScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is stale")
        }
        return mutableListOf<String>()
    }
}

data class TrailerScoreCodePrefixRule(var prefix: String) : TrailerScoreRule {
    override fun check(item: TrailerScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerScoreRequiredLabelRule(var message: String) : TrailerScoreRule {
    override fun check(item: TrailerScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer score. */
data class TrailerScoreValidator(var rules: MutableList<TrailerScoreRule>) {
    fun validate(item: TrailerScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerScoreValidator(): TrailerScoreValidator {
    return TrailerScoreValidator(
        mutableListOf<TrailerScoreRule>(
            TrailerScoreRequiredUnitRule("trailer score needs this field"),
            TrailerScoreArchivedGuardRule(3600),
            TrailerScoreAllowedReviewerRule(mutableListOf<String>("regional")),
            TrailerScoreCodePrefixRule("X"),
            TrailerScoreRequiredLabelRule("trailer score needs this field"),
        ),
    )
}
