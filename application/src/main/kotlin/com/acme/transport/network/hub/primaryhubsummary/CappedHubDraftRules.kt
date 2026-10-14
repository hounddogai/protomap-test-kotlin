package com.acme.transport.network.hub.primaryhubsummary

/** A rule a capped hub draft must pass. Check returns its problems, or none. */
interface CappedHubDraftRule {
    fun check(item: CappedHubDraft): MutableList<String>
}

data class CappedHubDraftTimezoneLengthRule(var maxLength: Int) : CappedHubDraftRule {
    override fun check(item: CappedHubDraft): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubDraftCodeLengthRule(var maxLength: Int) : CappedHubDraftRule {
    override fun check(item: CappedHubDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubDraftRequiredLocaleRule(var message: String) : CappedHubDraftRule {
    override fun check(item: CappedHubDraft): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped hub draft. */
data class CappedHubDraftValidator(var rules: MutableList<CappedHubDraftRule>) {
    fun validate(item: CappedHubDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedHubDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedHubDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedHubDraftValidator(): CappedHubDraftValidator {
    return CappedHubDraftValidator(
        mutableListOf<CappedHubDraftRule>(
            CappedHubDraftTimezoneLengthRule(80),
            CappedHubDraftCodeLengthRule(1000),
            CappedHubDraftRequiredLocaleRule("capped hub draft needs a value"),
        ),
    )
}
