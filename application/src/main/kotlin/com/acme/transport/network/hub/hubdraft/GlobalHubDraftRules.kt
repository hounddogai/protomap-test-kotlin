package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.normalize

/** A rule a global hub draft must pass. Check returns its problems, or none. */
interface GlobalHubDraftRule {
    fun check(item: GlobalHubDraft): MutableList<String>
}

data class GlobalHubDraftAmountRangeRule(var min: Double, var max: Double) : GlobalHubDraftRule {
    override fun check(item: GlobalHubDraft): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class GlobalHubDraftRequiredCodeRule(var message: String) : GlobalHubDraftRule {
    override fun check(item: GlobalHubDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubDraftCodeLengthRule(var maxLength: Int) : GlobalHubDraftRule {
    override fun check(item: GlobalHubDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubDraftTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : GlobalHubDraftRule {
    override fun check(item: GlobalHubDraft): MutableList<String> {
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

data class GlobalHubDraftRequiredCarrierRule(var message: String) : GlobalHubDraftRule {
    override fun check(item: GlobalHubDraft): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubDraftAllowedNameRule(var allowed: MutableList<String>) : GlobalHubDraftRule {
    override fun check(item: GlobalHubDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global hub draft. */
data class GlobalHubDraftValidator(var rules: MutableList<GlobalHubDraftRule>) {
    fun validate(item: GlobalHubDraft): MutableList<String> {
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

    fun requireValid(item: GlobalHubDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalHubDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalHubDraftValidator(): GlobalHubDraftValidator {
    return GlobalHubDraftValidator(
        mutableListOf<GlobalHubDraftRule>(
            GlobalHubDraftAmountRangeRule(8.5, 2032.9),
            GlobalHubDraftRequiredCodeRule("global hub draft needs an entry"),
            GlobalHubDraftCodeLengthRule(16),
            GlobalHubDraftTagsLimitRule(86400, mutableListOf<String>("regional", "lenient")),
            GlobalHubDraftRequiredCarrierRule("global hub draft needs an entry"),
            GlobalHubDraftAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
