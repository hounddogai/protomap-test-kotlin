package com.acme.transport.emissions.idling.group

/** A rule a idling proposal must pass. Check returns its problems, or none. */
interface IdlingProposalRule {
    fun check(item: IdlingProposal): MutableList<String>
}

data class IdlingProposalRatioRangeRule(var min: Double, var max: Double) : IdlingProposalRule {
    override fun check(item: IdlingProposal): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingProposalRequiredLabelRule(var message: String) : IdlingProposalRule {
    override fun check(item: IdlingProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingProposalDriverIdLengthRule(var maxLength: Int) : IdlingProposalRule {
    override fun check(item: IdlingProposal): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingProposalTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : IdlingProposalRule {
    override fun check(item: IdlingProposal): MutableList<String> {
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

data class IdlingProposalRequiredDriverIdRule(var message: String) : IdlingProposalRule {
    override fun check(item: IdlingProposal): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling proposal. */
data class IdlingProposalValidator(var rules: MutableList<IdlingProposalRule>) {
    fun validate(item: IdlingProposal): MutableList<String> {
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

    fun requireValid(item: IdlingProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingProposalValidator(): IdlingProposalValidator {
    return IdlingProposalValidator(
        mutableListOf<IdlingProposalRule>(
            IdlingProposalRatioRangeRule(4.8, 1326.9),
            IdlingProposalRequiredLabelRule("idling proposal needs this field"),
            IdlingProposalDriverIdLengthRule(80),
            IdlingProposalTagsLimitRule(120, mutableListOf<String>("strict", "rolling", "fallback")),
            IdlingProposalRequiredDriverIdRule("idling proposal needs a value"),
        ),
    )
}
