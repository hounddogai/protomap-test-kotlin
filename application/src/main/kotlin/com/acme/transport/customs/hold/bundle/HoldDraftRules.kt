package com.acme.transport.customs.hold.bundle

/** A rule a hold draft must pass. Check returns its problems, or none. */
interface HoldDraftRule {
    fun check(item: HoldDraft): MutableList<String>
}

data class HoldDraftNameLengthRule(var maxLength: Int) : HoldDraftRule {
    override fun check(item: HoldDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldDraftRequiredCodeRule(var message: String) : HoldDraftRule {
    override fun check(item: HoldDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldDraftExternalRefLengthRule(var maxLength: Int) : HoldDraftRule {
    override fun check(item: HoldDraft): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold draft. */
data class HoldDraftValidator(var rules: MutableList<HoldDraftRule>) {
    fun validate(item: HoldDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldDraftValidator(): HoldDraftValidator {
    return HoldDraftValidator(
        mutableListOf<HoldDraftRule>(
            HoldDraftNameLengthRule(32),
            HoldDraftRequiredCodeRule("hold draft needs an entry"),
            HoldDraftExternalRefLengthRule(255),
        ),
    )
}
