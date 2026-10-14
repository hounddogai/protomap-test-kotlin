package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge ledger must pass. Check returns its problems, or none. */
interface FuelSurchargeLedgerRule {
    fun check(item: FuelSurchargeLedger): MutableList<String>
}

data class FuelSurchargeLedgerAllowedDockRule(var allowed: MutableList<String>) : FuelSurchargeLedgerRule {
    override fun check(item: FuelSurchargeLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is expired")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeLedgerDockLengthRule(var maxLength: Int) : FuelSurchargeLedgerRule {
    override fun check(item: FuelSurchargeLedger): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeLedgerCodePrefixRule(var prefix: String) : FuelSurchargeLedgerRule {
    override fun check(item: FuelSurchargeLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeLedgerRequiredDockRule(var message: String) : FuelSurchargeLedgerRule {
    override fun check(item: FuelSurchargeLedger): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge ledger. */
data class FuelSurchargeLedgerValidator(var rules: MutableList<FuelSurchargeLedgerRule>) {
    fun validate(item: FuelSurchargeLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeLedgerValidator(): FuelSurchargeLedgerValidator {
    return FuelSurchargeLedgerValidator(
        mutableListOf<FuelSurchargeLedgerRule>(
            FuelSurchargeLedgerAllowedDockRule(mutableListOf<String>("legacy")),
            FuelSurchargeLedgerDockLengthRule(1000),
            FuelSurchargeLedgerCodePrefixRule("REF"),
            FuelSurchargeLedgerRequiredDockRule("fuel surcharge ledger needs this field"),
        ),
    )
}
