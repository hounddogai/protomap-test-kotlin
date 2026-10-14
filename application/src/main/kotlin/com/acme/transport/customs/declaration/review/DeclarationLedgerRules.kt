package com.acme.transport.customs.declaration.review

/** A rule a declaration ledger must pass. Check returns its problems, or none. */
interface DeclarationLedgerRule {
    fun check(item: DeclarationLedger): MutableList<String>
}

data class DeclarationLedgerRequiredChecksumRule(var message: String) : DeclarationLedgerRule {
    override fun check(item: DeclarationLedger): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationLedgerRequiredDriverIdRule(var message: String) : DeclarationLedgerRule {
    override fun check(item: DeclarationLedger): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationLedgerTitleLengthRule(var maxLength: Int) : DeclarationLedgerRule {
    override fun check(item: DeclarationLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationLedgerThresholdRangeRule(var min: Double, var max: Double) : DeclarationLedgerRule {
    override fun check(item: DeclarationLedger): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a declaration ledger. */
data class DeclarationLedgerValidator(var rules: MutableList<DeclarationLedgerRule>) {
    fun validate(item: DeclarationLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationLedgerValidator(): DeclarationLedgerValidator {
    return DeclarationLedgerValidator(
        mutableListOf<DeclarationLedgerRule>(
            DeclarationLedgerRequiredChecksumRule("declaration ledger needs this field"),
            DeclarationLedgerRequiredDriverIdRule("declaration ledger needs this field"),
            DeclarationLedgerTitleLengthRule(255),
            DeclarationLedgerThresholdRangeRule(6.7, 2845.2),
        ),
    )
}
