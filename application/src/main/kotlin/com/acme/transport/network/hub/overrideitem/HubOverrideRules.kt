package com.acme.transport.network.hub.overrideitem

/** A rule a hub override must pass. Check returns its problems, or none. */
interface HubOverrideRule {
    fun check(item: HubOverride): MutableList<String>
}

data class HubOverrideRequiredTitleRule(var message: String) : HubOverrideRule {
    override fun check(item: HubOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubOverrideTimezoneLengthRule(var maxLength: Int) : HubOverrideRule {
    override fun check(item: HubOverride): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubOverrideRequiredCurrencyRule(var message: String) : HubOverrideRule {
    override fun check(item: HubOverride): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub override. */
data class HubOverrideValidator(var rules: MutableList<HubOverrideRule>) {
    fun validate(item: HubOverride): MutableList<String> {
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

    fun requireValid(item: HubOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultHubOverrideValidator(): HubOverrideValidator {
    return HubOverrideValidator(
        mutableListOf<HubOverrideRule>(
            HubOverrideRequiredTitleRule("hub override needs an entry"),
            HubOverrideTimezoneLengthRule(255),
            HubOverrideRequiredCurrencyRule("hub override needs a value"),
        ),
    )
}
