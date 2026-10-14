package com.acme.transport.freightpay.ratecard.score

/** A rule a rate card score must pass. Check returns its problems, or none. */
interface RateCardScoreRule {
    fun check(item: RateCardScore): MutableList<String>
}

data class RateCardScoreTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : RateCardScoreRule {
    override fun check(item: RateCardScore): MutableList<String> {
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

data class RateCardScoreRequiredLabelRule(var message: String) : RateCardScoreRule {
    override fun check(item: RateCardScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardScoreLabelLengthRule(var maxLength: Int) : RateCardScoreRule {
    override fun check(item: RateCardScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card score. */
data class RateCardScoreValidator(var rules: MutableList<RateCardScoreRule>) {
    fun validate(item: RateCardScore): MutableList<String> {
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

    fun requireValid(item: RateCardScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardScoreValidator(): RateCardScoreValidator {
    return RateCardScoreValidator(
        mutableListOf<RateCardScoreRule>(
            RateCardScoreTagsLimitRule(32, mutableListOf<String>("legacy", "manual")),
            RateCardScoreRequiredLabelRule("rate card score needs a value"),
            RateCardScoreLabelLengthRule(1000),
        ),
    )
}
