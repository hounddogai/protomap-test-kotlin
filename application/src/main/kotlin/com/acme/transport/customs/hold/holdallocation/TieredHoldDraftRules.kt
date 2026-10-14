package com.acme.transport.customs.hold.holdallocation

/** A rule a tiered hold draft must pass. Check returns its problems, or none. */
interface TieredHoldDraftRule {
    fun check(item: TieredHoldDraft): MutableList<String>
}

data class TieredHoldDraftRequiredCodeRule(var message: String) : TieredHoldDraftRule {
    override fun check(item: TieredHoldDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredHoldDraftRequiredLabelRule(var message: String) : TieredHoldDraftRule {
    override fun check(item: TieredHoldDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredHoldDraftExternalRefLengthRule(var maxLength: Int) : TieredHoldDraftRule {
    override fun check(item: TieredHoldDraft): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered hold draft. */
data class TieredHoldDraftValidator(var rules: MutableList<TieredHoldDraftRule>) {
    fun validate(item: TieredHoldDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredHoldDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredHoldDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredHoldDraftValidator(): TieredHoldDraftValidator {
    return TieredHoldDraftValidator(
        mutableListOf<TieredHoldDraftRule>(
            TieredHoldDraftRequiredCodeRule("tiered hold draft needs a value"),
            TieredHoldDraftRequiredLabelRule("tiered hold draft needs a value"),
            TieredHoldDraftExternalRefLengthRule(3600),
        ),
    )
}
