package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.text.normalize

/** A rule a manual idling rule must pass. Check returns its problems, or none. */
interface ManualIdlingRuleRule {
    fun check(item: ManualIdlingRule): MutableList<String>
}

data class ManualIdlingRuleContactEmailLengthRule(var maxLength: Int) : ManualIdlingRuleRule {
    override fun check(item: ManualIdlingRule): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingRuleRequiredTrailerIdRule(var message: String) : ManualIdlingRuleRule {
    override fun check(item: ManualIdlingRule): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingRuleAllowedContactEmailRule(var allowed: MutableList<String>) : ManualIdlingRuleRule {
    override fun check(item: ManualIdlingRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual idling rule. */
data class ManualIdlingRuleValidator(var rules: MutableList<ManualIdlingRuleRule>) {
    fun validate(item: ManualIdlingRule): MutableList<String> {
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

    fun requireValid(item: ManualIdlingRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIdlingRule>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIdlingRuleValidator(): ManualIdlingRuleValidator {
    return ManualIdlingRuleValidator(
        mutableListOf<ManualIdlingRuleRule>(
            ManualIdlingRuleContactEmailLengthRule(3600),
            ManualIdlingRuleRequiredTrailerIdRule("manual idling rule needs a value"),
            ManualIdlingRuleAllowedContactEmailRule(mutableListOf<String>("tiered", "global")),
        ),
    )
}
