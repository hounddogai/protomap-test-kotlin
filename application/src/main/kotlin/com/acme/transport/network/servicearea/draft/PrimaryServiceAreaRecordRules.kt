package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.normalize

/** A rule a primary service area record must pass. Check returns its problems, or none. */
interface PrimaryServiceAreaRecordRule {
    fun check(item: PrimaryServiceAreaRecord): MutableList<String>
}

data class PrimaryServiceAreaRecordAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryServiceAreaRecordRule {
    override fun check(item: PrimaryServiceAreaRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaRecordRequiredLabelRule(var message: String) : PrimaryServiceAreaRecordRule {
    override fun check(item: PrimaryServiceAreaRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServiceAreaRecordSummaryLengthRule(var maxLength: Int) : PrimaryServiceAreaRecordRule {
    override fun check(item: PrimaryServiceAreaRecord): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary service area record. */
data class PrimaryServiceAreaRecordValidator(var rules: MutableList<PrimaryServiceAreaRecordRule>) {
    fun validate(item: PrimaryServiceAreaRecord): MutableList<String> {
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

    fun requireValid(item: PrimaryServiceAreaRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryServiceAreaRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryServiceAreaRecordValidator(): PrimaryServiceAreaRecordValidator {
    return PrimaryServiceAreaRecordValidator(
        mutableListOf<PrimaryServiceAreaRecordRule>(
            PrimaryServiceAreaRecordAllowedCodeRule(mutableListOf<String>("pending", "global")),
            PrimaryServiceAreaRecordRequiredLabelRule("primary service area record needs this field"),
            PrimaryServiceAreaRecordSummaryLengthRule(80),
        ),
    )
}
