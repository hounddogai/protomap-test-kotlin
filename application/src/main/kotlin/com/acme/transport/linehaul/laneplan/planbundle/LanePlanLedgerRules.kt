package com.acme.transport.linehaul.laneplan.planbundle

/** A rule a lane plan ledger must pass. Check returns its problems, or none. */
interface LanePlanLedgerRule {
    fun check(item: LanePlanLedger): MutableList<String>
}

data class LanePlanLedgerSourceLengthRule(var maxLength: Int) : LanePlanLedgerRule {
    override fun check(item: LanePlanLedger): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanLedgerGraceDaysRangeRule(var min: Int, var max: Int) : LanePlanLedgerRule {
    override fun check(item: LanePlanLedger): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class LanePlanLedgerCodeLengthRule(var maxLength: Int) : LanePlanLedgerRule {
    override fun check(item: LanePlanLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanLedgerPinnedGuardRule(var limit: Int) : LanePlanLedgerRule {
    override fun check(item: LanePlanLedger): MutableList<String> {
        if (item.pinned && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanLedgerSequenceRangeRule(var min: Int, var max: Int) : LanePlanLedgerRule {
    override fun check(item: LanePlanLedger): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan ledger. */
data class LanePlanLedgerValidator(var rules: MutableList<LanePlanLedgerRule>) {
    fun validate(item: LanePlanLedger): MutableList<String> {
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

    fun requireValid(item: LanePlanLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanLedgerValidator(): LanePlanLedgerValidator {
    return LanePlanLedgerValidator(
        mutableListOf<LanePlanLedgerRule>(
            LanePlanLedgerSourceLengthRule(32),
            LanePlanLedgerGraceDaysRangeRule(255, 255),
            LanePlanLedgerCodeLengthRule(1000),
            LanePlanLedgerPinnedGuardRule(120),
            LanePlanLedgerSequenceRangeRule(80, 86400),
        ),
    )
}
