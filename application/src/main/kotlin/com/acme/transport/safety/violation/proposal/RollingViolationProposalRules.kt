package com.acme.transport.safety.violation.proposal

/** A rule a rolling violation proposal must pass. Check returns its problems, or none. */
interface RollingViolationProposalRule {
    fun check(item: RollingViolationProposal): MutableList<String>
}

data class RollingViolationProposalContactEmailLengthRule(var maxLength: Int) : RollingViolationProposalRule {
    override fun check(item: RollingViolationProposal): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationProposalTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingViolationProposalRule {
    override fun check(item: RollingViolationProposal): MutableList<String> {
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

data class RollingViolationProposalRequiredCategoryRule(var message: String) : RollingViolationProposalRule {
    override fun check(item: RollingViolationProposal): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling violation proposal. */
data class RollingViolationProposalValidator(var rules: MutableList<RollingViolationProposalRule>) {
    fun validate(item: RollingViolationProposal): MutableList<String> {
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

    fun requireValid(item: RollingViolationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingViolationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingViolationProposalValidator(): RollingViolationProposalValidator {
    return RollingViolationProposalValidator(
        mutableListOf<RollingViolationProposalRule>(
            RollingViolationProposalContactEmailLengthRule(80),
            RollingViolationProposalTagsLimitRule(64, mutableListOf<String>("global", "rolling", "rolling")),
            RollingViolationProposalRequiredCategoryRule("rolling violation proposal needs a value"),
        ),
    )
}
