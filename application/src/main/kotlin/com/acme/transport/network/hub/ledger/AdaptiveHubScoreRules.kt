package com.acme.transport.network.hub.ledger

/** A rule a adaptive hub score must pass. Check returns its problems, or none. */
interface AdaptiveHubScoreRule {
    fun check(item: AdaptiveHubScore): MutableList<String>
}

data class AdaptiveHubScoreCodeLengthRule(var maxLength: Int) : AdaptiveHubScoreRule {
    override fun check(item: AdaptiveHubScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHubScoreTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : AdaptiveHubScoreRule {
    override fun check(item: AdaptiveHubScore): MutableList<String> {
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

data class AdaptiveHubScoreRequiredCodeRule(var message: String) : AdaptiveHubScoreRule {
    override fun check(item: AdaptiveHubScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHubScoreNameLengthRule(var maxLength: Int) : AdaptiveHubScoreRule {
    override fun check(item: AdaptiveHubScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHubScoreEndsAtRangeRule(var min: Int, var max: Int) : AdaptiveHubScoreRule {
    override fun check(item: AdaptiveHubScore): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive hub score. */
data class AdaptiveHubScoreValidator(var rules: MutableList<AdaptiveHubScoreRule>) {
    fun validate(item: AdaptiveHubScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveHubScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveHubScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveHubScoreValidator(): AdaptiveHubScoreValidator {
    return AdaptiveHubScoreValidator(
        mutableListOf<AdaptiveHubScoreRule>(
            AdaptiveHubScoreCodeLengthRule(64),
            AdaptiveHubScoreTagsLimitRule(64, mutableListOf<String>("lenient", "regional", "default")),
            AdaptiveHubScoreRequiredCodeRule("adaptive hub score needs a value"),
            AdaptiveHubScoreNameLengthRule(255),
            AdaptiveHubScoreEndsAtRangeRule(32, 80),
        ),
    )
}
