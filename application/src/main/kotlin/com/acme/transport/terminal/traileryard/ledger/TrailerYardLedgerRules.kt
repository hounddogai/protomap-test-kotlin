package com.acme.transport.terminal.traileryard.ledger

/** A rule a trailer yard ledger must pass. Check returns its problems, or none. */
interface TrailerYardLedgerRule {
    fun check(item: TrailerYardLedger): MutableList<String>
}

data class TrailerYardLedgerPalletsRangeRule(var min: Int, var max: Int) : TrailerYardLedgerRule {
    override fun check(item: TrailerYardLedger): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class TrailerYardLedgerRequiredNameRule(var message: String) : TrailerYardLedgerRule {
    override fun check(item: TrailerYardLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardLedgerRequiredCodeRule(var message: String) : TrailerYardLedgerRule {
    override fun check(item: TrailerYardLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardLedgerCodePrefixRule(var prefix: String) : TrailerYardLedgerRule {
    override fun check(item: TrailerYardLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardLedgerSegmentLengthRule(var maxLength: Int) : TrailerYardLedgerRule {
    override fun check(item: TrailerYardLedger): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardLedgerNameLengthRule(var maxLength: Int) : TrailerYardLedgerRule {
    override fun check(item: TrailerYardLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard ledger. */
data class TrailerYardLedgerValidator(var rules: MutableList<TrailerYardLedgerRule>) {
    fun validate(item: TrailerYardLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardLedgerValidator(): TrailerYardLedgerValidator {
    return TrailerYardLedgerValidator(
        mutableListOf<TrailerYardLedgerRule>(
            TrailerYardLedgerPalletsRangeRule(64, 86400),
            TrailerYardLedgerRequiredNameRule("trailer yard ledger needs an entry"),
            TrailerYardLedgerRequiredCodeRule("trailer yard ledger needs this field"),
            TrailerYardLedgerCodePrefixRule("REF"),
            TrailerYardLedgerSegmentLengthRule(120),
            TrailerYardLedgerNameLengthRule(16),
        ),
    )
}
