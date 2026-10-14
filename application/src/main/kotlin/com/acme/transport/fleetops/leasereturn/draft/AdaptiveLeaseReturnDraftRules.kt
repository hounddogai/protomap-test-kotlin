package com.acme.transport.fleetops.leasereturn.draft

/** A rule a adaptive lease return draft must pass. Check returns its problems, or none. */
interface AdaptiveLeaseReturnDraftRule {
    fun check(item: AdaptiveLeaseReturnDraft): MutableList<String>
}

data class AdaptiveLeaseReturnDraftTrailerIdLengthRule(var maxLength: Int) : AdaptiveLeaseReturnDraftRule {
    override fun check(item: AdaptiveLeaseReturnDraft): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnDraftCodePrefixRule(var prefix: String) : AdaptiveLeaseReturnDraftRule {
    override fun check(item: AdaptiveLeaseReturnDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnDraftRequiredSourceRule(var message: String) : AdaptiveLeaseReturnDraftRule {
    override fun check(item: AdaptiveLeaseReturnDraft): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lease return draft. */
data class AdaptiveLeaseReturnDraftValidator(var rules: MutableList<AdaptiveLeaseReturnDraftRule>) {
    fun validate(item: AdaptiveLeaseReturnDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveLeaseReturnDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLeaseReturnDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLeaseReturnDraftValidator(): AdaptiveLeaseReturnDraftValidator {
    return AdaptiveLeaseReturnDraftValidator(
        mutableListOf<AdaptiveLeaseReturnDraftRule>(
            AdaptiveLeaseReturnDraftTrailerIdLengthRule(64),
            AdaptiveLeaseReturnDraftCodePrefixRule("ACM"),
            AdaptiveLeaseReturnDraftRequiredSourceRule("adaptive lease return draft needs an entry"),
        ),
    )
}
