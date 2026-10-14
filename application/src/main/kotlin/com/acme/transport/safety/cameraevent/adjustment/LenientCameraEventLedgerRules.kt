package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a lenient camera event ledger must pass. Check returns its problems, or none. */
interface LenientCameraEventLedgerRule {
    fun check(item: LenientCameraEventLedger): MutableList<String>
}

data class LenientCameraEventLedgerLabelLengthRule(var maxLength: Int) : LenientCameraEventLedgerRule {
    override fun check(item: LenientCameraEventLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventLedgerQuantityRangeRule(
    var min: Int,
    var max: Int,
) : LenientCameraEventLedgerRule {
    override fun check(item: LenientCameraEventLedger): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class LenientCameraEventLedgerRequiredLabelRule(var message: String) : LenientCameraEventLedgerRule {
    override fun check(item: LenientCameraEventLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventLedgerCodeLengthRule(var maxLength: Int) : LenientCameraEventLedgerRule {
    override fun check(item: LenientCameraEventLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventLedgerVisibleGuardRule(var limit: Double) : LenientCameraEventLedgerRule {
    override fun check(item: LenientCameraEventLedger): MutableList<String> {
        if (item.visible && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventLedgerAllowedCountryRule(
    var allowed: MutableList<String>,
) : LenientCameraEventLedgerRule {
    override fun check(item: LenientCameraEventLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient camera event ledger. */
data class LenientCameraEventLedgerValidator(var rules: MutableList<LenientCameraEventLedgerRule>) {
    fun validate(item: LenientCameraEventLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientCameraEventLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCameraEventLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCameraEventLedgerValidator(): LenientCameraEventLedgerValidator {
    return LenientCameraEventLedgerValidator(
        mutableListOf<LenientCameraEventLedgerRule>(
            LenientCameraEventLedgerLabelLengthRule(16),
            LenientCameraEventLedgerQuantityRangeRule(32, 64),
            LenientCameraEventLedgerRequiredLabelRule("lenient camera event ledger needs an entry"),
            LenientCameraEventLedgerCodeLengthRule(64),
            LenientCameraEventLedgerVisibleGuardRule(3.4),
            LenientCameraEventLedgerAllowedCountryRule(mutableListOf<String>("tiered")),
        ),
    )
}
