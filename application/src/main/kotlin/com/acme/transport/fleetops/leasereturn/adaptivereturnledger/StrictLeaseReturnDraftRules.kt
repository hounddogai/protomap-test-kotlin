package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

/** A rule a strict lease return draft must pass. Check returns its problems, or none. */
interface StrictLeaseReturnDraftRule {
    fun check(item: StrictLeaseReturnDraft): MutableList<String>
}

data class StrictLeaseReturnDraftLabelLengthRule(var maxLength: Int) : StrictLeaseReturnDraftRule {
    override fun check(item: StrictLeaseReturnDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictLeaseReturnDraftRequiredCarrierRule(var message: String) : StrictLeaseReturnDraftRule {
    override fun check(item: StrictLeaseReturnDraft): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictLeaseReturnDraftTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : StrictLeaseReturnDraftRule {
    override fun check(item: StrictLeaseReturnDraft): MutableList<String> {
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

/** Runs every rule of a strict lease return draft. */
data class StrictLeaseReturnDraftValidator(var rules: MutableList<StrictLeaseReturnDraftRule>) {
    fun validate(item: StrictLeaseReturnDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictLeaseReturnDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictLeaseReturnDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictLeaseReturnDraftValidator(): StrictLeaseReturnDraftValidator {
    return StrictLeaseReturnDraftValidator(
        mutableListOf<StrictLeaseReturnDraftRule>(
            StrictLeaseReturnDraftLabelLengthRule(80),
            StrictLeaseReturnDraftRequiredCarrierRule("strict lease return draft needs a value"),
            StrictLeaseReturnDraftTagsLimitRule(32, mutableListOf<String>("lenient")),
        ),
    )
}
