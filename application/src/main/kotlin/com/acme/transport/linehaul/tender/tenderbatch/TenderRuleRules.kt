package com.acme.transport.linehaul.tender.tenderbatch

/** A rule a tender rule must pass. Check returns its problems, or none. */
interface TenderRuleRule {
    fun check(item: TenderRule): MutableList<String>
}

data class TenderRuleRequiredReviewerRule(var message: String) : TenderRuleRule {
    override fun check(item: TenderRule): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderRuleCodeLengthRule(var maxLength: Int) : TenderRuleRule {
    override fun check(item: TenderRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderRuleRequiredCodeRule(var message: String) : TenderRuleRule {
    override fun check(item: TenderRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender rule. */
data class TenderRuleValidator(var rules: MutableList<TenderRuleRule>) {
    fun validate(item: TenderRule): MutableList<String> {
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

    fun requireValid(item: TenderRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderRuleValidator(): TenderRuleValidator {
    return TenderRuleValidator(
        mutableListOf<TenderRuleRule>(
            TenderRuleRequiredReviewerRule("tender rule needs this field"),
            TenderRuleCodeLengthRule(80),
            TenderRuleRequiredCodeRule("tender rule needs this field"),
        ),
    )
}
