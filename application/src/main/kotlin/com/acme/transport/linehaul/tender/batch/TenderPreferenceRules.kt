package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.text.normalize

/** A rule a tender preference must pass. Check returns its problems, or none. */
interface TenderPreferenceRule {
    fun check(item: TenderPreference): MutableList<String>
}

data class TenderPreferenceRequiredLabelRule(var message: String) : TenderPreferenceRule {
    override fun check(item: TenderPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderPreferenceAllowedCodeRule(var allowed: MutableList<String>) : TenderPreferenceRule {
    override fun check(item: TenderPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TenderPreferenceRequiredCurrencyRule(var message: String) : TenderPreferenceRule {
    override fun check(item: TenderPreference): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender preference. */
data class TenderPreferenceValidator(var rules: MutableList<TenderPreferenceRule>) {
    fun validate(item: TenderPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderPreferenceValidator(): TenderPreferenceValidator {
    return TenderPreferenceValidator(
        mutableListOf<TenderPreferenceRule>(
            TenderPreferenceRequiredLabelRule("tender preference needs an entry"),
            TenderPreferenceAllowedCodeRule(mutableListOf<String>()),
            TenderPreferenceRequiredCurrencyRule("tender preference needs this field"),
        ),
    )
}
