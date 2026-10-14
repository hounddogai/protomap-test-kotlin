package com.acme.transport.lastmile.proofofdelivery.review

/** A rule a default proof of delivery ledger must pass. Check returns its problems, or none. */
interface DefaultProofOfDeliveryLedgerRule {
    fun check(item: DefaultProofOfDeliveryLedger): MutableList<String>
}

data class DefaultProofOfDeliveryLedgerPalletsRangeRule(
    var min: Int,
    var max: Int,
) : DefaultProofOfDeliveryLedgerRule {
    override fun check(item: DefaultProofOfDeliveryLedger): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryLedgerCodeLengthRule(var maxLength: Int) : DefaultProofOfDeliveryLedgerRule {
    override fun check(item: DefaultProofOfDeliveryLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryLedgerRouteCodeLengthRule(
    var maxLength: Int,
) : DefaultProofOfDeliveryLedgerRule {
    override fun check(item: DefaultProofOfDeliveryLedger): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryLedgerCodePrefixRule(var prefix: String) : DefaultProofOfDeliveryLedgerRule {
    override fun check(item: DefaultProofOfDeliveryLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default proof of delivery ledger. */
data class DefaultProofOfDeliveryLedgerValidator(var rules: MutableList<DefaultProofOfDeliveryLedgerRule>) {
    fun validate(item: DefaultProofOfDeliveryLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultProofOfDeliveryLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultProofOfDeliveryLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultProofOfDeliveryLedgerValidator(): DefaultProofOfDeliveryLedgerValidator {
    return DefaultProofOfDeliveryLedgerValidator(
        mutableListOf<DefaultProofOfDeliveryLedgerRule>(
            DefaultProofOfDeliveryLedgerPalletsRangeRule(80, 3600),
            DefaultProofOfDeliveryLedgerCodeLengthRule(64),
            DefaultProofOfDeliveryLedgerRouteCodeLengthRule(120),
            DefaultProofOfDeliveryLedgerCodePrefixRule("Q"),
        ),
    )
}
