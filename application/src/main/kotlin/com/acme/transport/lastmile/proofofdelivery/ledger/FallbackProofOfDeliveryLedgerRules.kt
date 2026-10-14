package com.acme.transport.lastmile.proofofdelivery.ledger

/** A rule a fallback proof of delivery ledger must pass. Check returns its problems, or none. */
interface FallbackProofOfDeliveryLedgerRule {
    fun check(item: FallbackProofOfDeliveryLedger): MutableList<String>
}

data class FallbackProofOfDeliveryLedgerCodePrefixRule(var prefix: String) : FallbackProofOfDeliveryLedgerRule {
    override fun check(item: FallbackProofOfDeliveryLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackProofOfDeliveryLedgerFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : FallbackProofOfDeliveryLedgerRule {
    override fun check(item: FallbackProofOfDeliveryLedger): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackProofOfDeliveryLedgerActiveGuardRule(var limit: Double) : FallbackProofOfDeliveryLedgerRule {
    override fun check(item: FallbackProofOfDeliveryLedger): MutableList<String> {
        if (item.active && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class FallbackProofOfDeliveryLedgerContactEmailLengthRule(
    var maxLength: Int,
) : FallbackProofOfDeliveryLedgerRule {
    override fun check(item: FallbackProofOfDeliveryLedger): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackProofOfDeliveryLedgerRequiredContactEmailRule(
    var message: String,
) : FallbackProofOfDeliveryLedgerRule {
    override fun check(item: FallbackProofOfDeliveryLedger): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback proof of delivery ledger. */
data class FallbackProofOfDeliveryLedgerValidator(var rules: MutableList<FallbackProofOfDeliveryLedgerRule>) {
    fun validate(item: FallbackProofOfDeliveryLedger): MutableList<String> {
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

    fun requireValid(item: FallbackProofOfDeliveryLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackProofOfDeliveryLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackProofOfDeliveryLedgerValidator(): FallbackProofOfDeliveryLedgerValidator {
    return FallbackProofOfDeliveryLedgerValidator(
        mutableListOf<FallbackProofOfDeliveryLedgerRule>(
            FallbackProofOfDeliveryLedgerCodePrefixRule("X"),
            FallbackProofOfDeliveryLedgerFuelLitersRangeRule(8.5, 1316.2),
            FallbackProofOfDeliveryLedgerActiveGuardRule(0.4),
            FallbackProofOfDeliveryLedgerContactEmailLengthRule(1000),
            FallbackProofOfDeliveryLedgerRequiredContactEmailRule(
                "fallback proof of delivery ledger needs this field",
            ),
        ),
    )
}
