package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a weighted hub draft must pass. Check returns its problems, or none. */
interface WeightedHubDraftRule {
    fun check(item: WeightedHubDraft): MutableList<String>
}

data class WeightedHubDraftRequiredLabelRule(var message: String) : WeightedHubDraftRule {
    override fun check(item: WeightedHubDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubDraftRequiredTitleRule(var message: String) : WeightedHubDraftRule {
    override fun check(item: WeightedHubDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubDraftCodeLengthRule(var maxLength: Int) : WeightedHubDraftRule {
    override fun check(item: WeightedHubDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubDraftRequiredTrailerIdRule(var message: String) : WeightedHubDraftRule {
    override fun check(item: WeightedHubDraft): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubDraftAllowedChecksumRule(var allowed: MutableList<String>) : WeightedHubDraftRule {
    override fun check(item: WeightedHubDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted hub draft. */
data class WeightedHubDraftValidator(var rules: MutableList<WeightedHubDraftRule>) {
    fun validate(item: WeightedHubDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedHubDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedHubDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedHubDraftValidator(): WeightedHubDraftValidator {
    return WeightedHubDraftValidator(
        mutableListOf<WeightedHubDraftRule>(
            WeightedHubDraftRequiredLabelRule("weighted hub draft needs a value"),
            WeightedHubDraftRequiredTitleRule("weighted hub draft needs an entry"),
            WeightedHubDraftCodeLengthRule(255),
            WeightedHubDraftRequiredTrailerIdRule("weighted hub draft needs an entry"),
            WeightedHubDraftAllowedChecksumRule(mutableListOf<String>("scheduled", "primary", "strict")),
        ),
    )
}
