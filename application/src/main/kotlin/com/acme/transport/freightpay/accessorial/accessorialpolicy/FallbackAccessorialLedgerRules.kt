package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.text.normalize

/** A rule a fallback accessorial ledger must pass. Check returns its problems, or none. */
interface FallbackAccessorialLedgerRule {
    fun check(item: FallbackAccessorialLedger): MutableList<String>
}

data class FallbackAccessorialLedgerAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : FallbackAccessorialLedgerRule {
    override fun check(item: FallbackAccessorialLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is expired")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialLedgerOdometerRangeRule(
    var min: Int,
    var max: Int,
) : FallbackAccessorialLedgerRule {
    override fun check(item: FallbackAccessorialLedger): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialLedgerRequiredOwnerIdRule(var message: String) : FallbackAccessorialLedgerRule {
    override fun check(item: FallbackAccessorialLedger): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialLedgerRequiredPhoneRule(var message: String) : FallbackAccessorialLedgerRule {
    override fun check(item: FallbackAccessorialLedger): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialLedgerOwnerIdLengthRule(var maxLength: Int) : FallbackAccessorialLedgerRule {
    override fun check(item: FallbackAccessorialLedger): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialLedgerPhoneLengthRule(var maxLength: Int) : FallbackAccessorialLedgerRule {
    override fun check(item: FallbackAccessorialLedger): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback accessorial ledger. */
data class FallbackAccessorialLedgerValidator(var rules: MutableList<FallbackAccessorialLedgerRule>) {
    fun validate(item: FallbackAccessorialLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackAccessorialLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAccessorialLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAccessorialLedgerValidator(): FallbackAccessorialLedgerValidator {
    return FallbackAccessorialLedgerValidator(
        mutableListOf<FallbackAccessorialLedgerRule>(
            FallbackAccessorialLedgerAllowedOwnerIdRule(mutableListOf<String>("legacy")),
            FallbackAccessorialLedgerOdometerRangeRule(16, 64),
            FallbackAccessorialLedgerRequiredOwnerIdRule("fallback accessorial ledger needs this field"),
            FallbackAccessorialLedgerRequiredPhoneRule("fallback accessorial ledger needs a value"),
            FallbackAccessorialLedgerOwnerIdLengthRule(120),
            FallbackAccessorialLedgerPhoneLengthRule(3600),
        ),
    )
}
