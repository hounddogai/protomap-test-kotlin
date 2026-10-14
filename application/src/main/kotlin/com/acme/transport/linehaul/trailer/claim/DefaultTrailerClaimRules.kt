package com.acme.transport.linehaul.trailer.claim

/** A rule a default trailer claim must pass. Check returns its problems, or none. */
interface DefaultTrailerClaimRule {
    fun check(item: DefaultTrailerClaim): MutableList<String>
}

data class DefaultTrailerClaimRequiredDepotRule(var message: String) : DefaultTrailerClaimRule {
    override fun check(item: DefaultTrailerClaim): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerClaimLabelLengthRule(var maxLength: Int) : DefaultTrailerClaimRule {
    override fun check(item: DefaultTrailerClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerClaimDepotLengthRule(var maxLength: Int) : DefaultTrailerClaimRule {
    override fun check(item: DefaultTrailerClaim): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default trailer claim. */
data class DefaultTrailerClaimValidator(var rules: MutableList<DefaultTrailerClaimRule>) {
    fun validate(item: DefaultTrailerClaim): MutableList<String> {
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

    fun requireValid(item: DefaultTrailerClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTrailerClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTrailerClaimValidator(): DefaultTrailerClaimValidator {
    return DefaultTrailerClaimValidator(
        mutableListOf<DefaultTrailerClaimRule>(
            DefaultTrailerClaimRequiredDepotRule("default trailer claim needs this field"),
            DefaultTrailerClaimLabelLengthRule(16),
            DefaultTrailerClaimDepotLengthRule(80),
        ),
    )
}
