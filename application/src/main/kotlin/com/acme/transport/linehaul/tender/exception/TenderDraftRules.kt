package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.text.normalize

/** A rule a tender draft must pass. Check returns its problems, or none. */
interface TenderDraftRule {
    fun check(item: TenderDraft): MutableList<String>
}

data class TenderDraftAllowedNameRule(var allowed: MutableList<String>) : TenderDraftRule {
    override fun check(item: TenderDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class TenderDraftRequiredNameRule(var message: String) : TenderDraftRule {
    override fun check(item: TenderDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderDraftWindowDaysRangeRule(var min: Int, var max: Int) : TenderDraftRule {
    override fun check(item: TenderDraft): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderDraftNameLengthRule(var maxLength: Int) : TenderDraftRule {
    override fun check(item: TenderDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderDraftLabelLengthRule(var maxLength: Int) : TenderDraftRule {
    override fun check(item: TenderDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderDraftCodeLengthRule(var maxLength: Int) : TenderDraftRule {
    override fun check(item: TenderDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender draft. */
data class TenderDraftValidator(var rules: MutableList<TenderDraftRule>) {
    fun validate(item: TenderDraft): MutableList<String> {
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

    fun requireValid(item: TenderDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderDraftValidator(): TenderDraftValidator {
    return TenderDraftValidator(
        mutableListOf<TenderDraftRule>(
            TenderDraftAllowedNameRule(mutableListOf<String>("scheduled", "rolling", "global")),
            TenderDraftRequiredNameRule("tender draft needs a value"),
            TenderDraftWindowDaysRangeRule(16, 32),
            TenderDraftNameLengthRule(3600),
            TenderDraftLabelLengthRule(86400),
            TenderDraftCodeLengthRule(80),
        ),
    )
}
