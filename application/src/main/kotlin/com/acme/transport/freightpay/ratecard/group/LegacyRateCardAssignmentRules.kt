package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.normalize

/** A rule a legacy rate card assignment must pass. Check returns its problems, or none. */
interface LegacyRateCardAssignmentRule {
    fun check(item: LegacyRateCardAssignment): MutableList<String>
}

data class LegacyRateCardAssignmentCodePrefixRule(var prefix: String) : LegacyRateCardAssignmentRule {
    override fun check(item: LegacyRateCardAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardAssignmentAllowedNameRule(
    var allowed: MutableList<String>,
) : LegacyRateCardAssignmentRule {
    override fun check(item: LegacyRateCardAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardAssignmentNotesLengthRule(var maxLength: Int) : LegacyRateCardAssignmentRule {
    override fun check(item: LegacyRateCardAssignment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardAssignmentRequiredNameRule(var message: String) : LegacyRateCardAssignmentRule {
    override fun check(item: LegacyRateCardAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardAssignmentNameLengthRule(var maxLength: Int) : LegacyRateCardAssignmentRule {
    override fun check(item: LegacyRateCardAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardAssignmentRequiredCategoryRule(var message: String) : LegacyRateCardAssignmentRule {
    override fun check(item: LegacyRateCardAssignment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy rate card assignment. */
data class LegacyRateCardAssignmentValidator(var rules: MutableList<LegacyRateCardAssignmentRule>) {
    fun validate(item: LegacyRateCardAssignment): MutableList<String> {
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

    fun requireValid(item: LegacyRateCardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRateCardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRateCardAssignmentValidator(): LegacyRateCardAssignmentValidator {
    return LegacyRateCardAssignmentValidator(
        mutableListOf<LegacyRateCardAssignmentRule>(
            LegacyRateCardAssignmentCodePrefixRule("X"),
            LegacyRateCardAssignmentAllowedNameRule(mutableListOf<String>()),
            LegacyRateCardAssignmentNotesLengthRule(86400),
            LegacyRateCardAssignmentRequiredNameRule("legacy rate card assignment needs an entry"),
            LegacyRateCardAssignmentNameLengthRule(32),
            LegacyRateCardAssignmentRequiredCategoryRule("legacy rate card assignment needs an entry"),
        ),
    )
}
