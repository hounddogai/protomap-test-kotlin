package com.acme.transport.lastmile.locker.policy

/** A rule a locker ledger must pass. Check returns its problems, or none. */
interface LockerLedgerRule {
    fun check(item: LockerLedger): MutableList<String>
}

data class LockerLedgerRequiredTitleRule(var message: String) : LockerLedgerRule {
    override fun check(item: LockerLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerLedgerWeightRangeRule(var min: Double, var max: Double) : LockerLedgerRule {
    override fun check(item: LockerLedger): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerLedgerRequiredCodeRule(var message: String) : LockerLedgerRule {
    override fun check(item: LockerLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerLedgerCodeLengthRule(var maxLength: Int) : LockerLedgerRule {
    override fun check(item: LockerLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerLedgerQuantityRangeRule(var min: Int, var max: Int) : LockerLedgerRule {
    override fun check(item: LockerLedger): MutableList<String> {
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

data class LockerLedgerTitleLengthRule(var maxLength: Int) : LockerLedgerRule {
    override fun check(item: LockerLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker ledger. */
data class LockerLedgerValidator(var rules: MutableList<LockerLedgerRule>) {
    fun validate(item: LockerLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerLedgerValidator(): LockerLedgerValidator {
    return LockerLedgerValidator(
        mutableListOf<LockerLedgerRule>(
            LockerLedgerRequiredTitleRule("locker ledger needs a value"),
            LockerLedgerWeightRangeRule(5.0, 147.4),
            LockerLedgerRequiredCodeRule("locker ledger needs an entry"),
            LockerLedgerCodeLengthRule(255),
            LockerLedgerQuantityRangeRule(64, 64),
            LockerLedgerTitleLengthRule(1000),
        ),
    )
}
