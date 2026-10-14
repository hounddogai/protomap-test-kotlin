package com.acme.transport.crewing.qualification.variance

import com.acme.transport.kit.text.normalize

/** A rule a qualification ledger must pass. Check returns its problems, or none. */
interface QualificationLedgerRule {
    fun check(item: QualificationLedger): MutableList<String>
}

data class QualificationLedgerChannelLengthRule(var maxLength: Int) : QualificationLedgerRule {
    override fun check(item: QualificationLedger): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationLedgerAllowedCodeRule(var allowed: MutableList<String>) : QualificationLedgerRule {
    override fun check(item: QualificationLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class QualificationLedgerCodePrefixRule(var prefix: String) : QualificationLedgerRule {
    override fun check(item: QualificationLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationLedgerRequiredCodeRule(var message: String) : QualificationLedgerRule {
    override fun check(item: QualificationLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationLedgerPalletsRangeRule(var min: Int, var max: Int) : QualificationLedgerRule {
    override fun check(item: QualificationLedger): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification ledger. */
data class QualificationLedgerValidator(var rules: MutableList<QualificationLedgerRule>) {
    fun validate(item: QualificationLedger): MutableList<String> {
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

    fun requireValid(item: QualificationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationLedgerValidator(): QualificationLedgerValidator {
    return QualificationLedgerValidator(
        mutableListOf<QualificationLedgerRule>(
            QualificationLedgerChannelLengthRule(32),
            QualificationLedgerAllowedCodeRule(mutableListOf<String>()),
            QualificationLedgerCodePrefixRule("X"),
            QualificationLedgerRequiredCodeRule("qualification ledger needs an entry"),
            QualificationLedgerPalletsRangeRule(80, 120),
        ),
    )
}
