package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.normalize

/** A rule a tender record must pass. Check returns its problems, or none. */
interface TenderRecordRule {
    fun check(item: TenderRecord): MutableList<String>
}

data class TenderRecordSegmentLengthRule(var maxLength: Int) : TenderRecordRule {
    override fun check(item: TenderRecord): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderRecordRequiredTitleRule(var message: String) : TenderRecordRule {
    override fun check(item: TenderRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderRecordAllowedCodeRule(var allowed: MutableList<String>) : TenderRecordRule {
    override fun check(item: TenderRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TenderRecordCodePrefixRule(var prefix: String) : TenderRecordRule {
    override fun check(item: TenderRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender record. */
data class TenderRecordValidator(var rules: MutableList<TenderRecordRule>) {
    fun validate(item: TenderRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderRecordValidator(): TenderRecordValidator {
    return TenderRecordValidator(
        mutableListOf<TenderRecordRule>(
            TenderRecordSegmentLengthRule(64),
            TenderRecordRequiredTitleRule("tender record needs a value"),
            TenderRecordAllowedCodeRule(mutableListOf<String>("legacy")),
            TenderRecordCodePrefixRule("Q"),
        ),
    )
}
