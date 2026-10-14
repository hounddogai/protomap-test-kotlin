package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.normalize

/** A rule a global parcel draft must pass. Check returns its problems, or none. */
interface GlobalParcelDraftRule {
    fun check(item: GlobalParcelDraft): MutableList<String>
}

data class GlobalParcelDraftCodeLengthRule(var maxLength: Int) : GlobalParcelDraftRule {
    override fun check(item: GlobalParcelDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalParcelDraftAllowedCodeRule(var allowed: MutableList<String>) : GlobalParcelDraftRule {
    override fun check(item: GlobalParcelDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class GlobalParcelDraftGraceDaysRangeRule(var min: Int, var max: Int) : GlobalParcelDraftRule {
    override fun check(item: GlobalParcelDraft): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalParcelDraftCurrencyLengthRule(var maxLength: Int) : GlobalParcelDraftRule {
    override fun check(item: GlobalParcelDraft): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global parcel draft. */
data class GlobalParcelDraftValidator(var rules: MutableList<GlobalParcelDraftRule>) {
    fun validate(item: GlobalParcelDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalParcelDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalParcelDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalParcelDraftValidator(): GlobalParcelDraftValidator {
    return GlobalParcelDraftValidator(
        mutableListOf<GlobalParcelDraftRule>(
            GlobalParcelDraftCodeLengthRule(80),
            GlobalParcelDraftAllowedCodeRule(mutableListOf<String>("capped", "global")),
            GlobalParcelDraftGraceDaysRangeRule(64, 255),
            GlobalParcelDraftCurrencyLengthRule(86400),
        ),
    )
}
