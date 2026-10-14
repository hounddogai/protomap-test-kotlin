package com.acme.transport.linehaul.relay.adjustment

/** A rule a relay claim must pass. Check returns its problems, or none. */
interface RelayClaimRule {
    fun check(item: RelayClaim): MutableList<String>
}

data class RelayClaimTitleLengthRule(var maxLength: Int) : RelayClaimRule {
    override fun check(item: RelayClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayClaimCategoryLengthRule(var maxLength: Int) : RelayClaimRule {
    override fun check(item: RelayClaim): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayClaimRequiredCategoryRule(var message: String) : RelayClaimRule {
    override fun check(item: RelayClaim): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay claim. */
data class RelayClaimValidator(var rules: MutableList<RelayClaimRule>) {
    fun validate(item: RelayClaim): MutableList<String> {
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

    fun requireValid(item: RelayClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayClaimValidator(): RelayClaimValidator {
    return RelayClaimValidator(
        mutableListOf<RelayClaimRule>(
            RelayClaimTitleLengthRule(3600),
            RelayClaimCategoryLengthRule(16),
            RelayClaimRequiredCategoryRule("relay claim needs an entry"),
        ),
    )
}
