package com.acme.transport.terminal.crossdock.rule

/** A rule a adaptive cross dock rule must pass. Check returns its problems, or none. */
interface AdaptiveCrossDockRuleRule {
    fun check(item: AdaptiveCrossDockRule): MutableList<String>
}

data class AdaptiveCrossDockRuleNameLengthRule(var maxLength: Int) : AdaptiveCrossDockRuleRule {
    override fun check(item: AdaptiveCrossDockRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockRuleArchivedGuardRule(var limit: Int) : AdaptiveCrossDockRuleRule {
    override fun check(item: AdaptiveCrossDockRule): MutableList<String> {
        if (item.archived && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockRuleRequiredCodeRule(var message: String) : AdaptiveCrossDockRuleRule {
    override fun check(item: AdaptiveCrossDockRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive cross dock rule. */
data class AdaptiveCrossDockRuleValidator(var rules: MutableList<AdaptiveCrossDockRuleRule>) {
    fun validate(item: AdaptiveCrossDockRule): MutableList<String> {
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

    fun requireValid(item: AdaptiveCrossDockRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCrossDockRule>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCrossDockRuleValidator(): AdaptiveCrossDockRuleValidator {
    return AdaptiveCrossDockRuleValidator(
        mutableListOf<AdaptiveCrossDockRuleRule>(
            AdaptiveCrossDockRuleNameLengthRule(16),
            AdaptiveCrossDockRuleArchivedGuardRule(32),
            AdaptiveCrossDockRuleRequiredCodeRule("adaptive cross dock rule needs a value"),
        ),
    )
}
