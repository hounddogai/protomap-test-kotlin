package com.acme.transport.crewing.roster.recorditem

/** A rule a lenient roster ledger must pass. Check returns its problems, or none. */
interface LenientRosterLedgerRule {
    fun check(item: LenientRosterLedger): MutableList<String>
}

data class LenientRosterLedgerRequiredCodeRule(var message: String) : LenientRosterLedgerRule {
    override fun check(item: LenientRosterLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterLedgerRatioRangeRule(var min: Double, var max: Double) : LenientRosterLedgerRule {
    override fun check(item: LenientRosterLedger): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterLedgerFloorValueRangeRule(var min: Double, var max: Double) : LenientRosterLedgerRule {
    override fun check(item: LenientRosterLedger): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class LenientRosterLedgerRetriesRangeRule(var min: Int, var max: Int) : LenientRosterLedgerRule {
    override fun check(item: LenientRosterLedger): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class LenientRosterLedgerCodeLengthRule(var maxLength: Int) : LenientRosterLedgerRule {
    override fun check(item: LenientRosterLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterLedgerChannelLengthRule(var maxLength: Int) : LenientRosterLedgerRule {
    override fun check(item: LenientRosterLedger): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient roster ledger. */
data class LenientRosterLedgerValidator(var rules: MutableList<LenientRosterLedgerRule>) {
    fun validate(item: LenientRosterLedger): MutableList<String> {
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

    fun requireValid(item: LenientRosterLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRosterLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRosterLedgerValidator(): LenientRosterLedgerValidator {
    return LenientRosterLedgerValidator(
        mutableListOf<LenientRosterLedgerRule>(
            LenientRosterLedgerRequiredCodeRule("lenient roster ledger needs an entry"),
            LenientRosterLedgerRatioRangeRule(3.3, 4765.6),
            LenientRosterLedgerFloorValueRangeRule(4.2, 4490.5),
            LenientRosterLedgerRetriesRangeRule(120, 255),
            LenientRosterLedgerCodeLengthRule(120),
            LenientRosterLedgerChannelLengthRule(120),
        ),
    )
}
