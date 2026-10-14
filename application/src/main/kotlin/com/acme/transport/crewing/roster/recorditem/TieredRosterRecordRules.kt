package com.acme.transport.crewing.roster.recorditem

/** A rule a tiered roster record must pass. Check returns its problems, or none. */
interface TieredRosterRecordRule {
    fun check(item: TieredRosterRecord): MutableList<String>
}

data class TieredRosterRecordDockLengthRule(var maxLength: Int) : TieredRosterRecordRule {
    override fun check(item: TieredRosterRecord): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterRecordCodeLengthRule(var maxLength: Int) : TieredRosterRecordRule {
    override fun check(item: TieredRosterRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterRecordRequiredCodeRule(var message: String) : TieredRosterRecordRule {
    override fun check(item: TieredRosterRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterRecordRequiredNameRule(var message: String) : TieredRosterRecordRule {
    override fun check(item: TieredRosterRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered roster record. */
data class TieredRosterRecordValidator(var rules: MutableList<TieredRosterRecordRule>) {
    fun validate(item: TieredRosterRecord): MutableList<String> {
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

    fun requireValid(item: TieredRosterRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRosterRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRosterRecordValidator(): TieredRosterRecordValidator {
    return TieredRosterRecordValidator(
        mutableListOf<TieredRosterRecordRule>(
            TieredRosterRecordDockLengthRule(16),
            TieredRosterRecordCodeLengthRule(3600),
            TieredRosterRecordRequiredCodeRule("tiered roster record needs this field"),
            TieredRosterRecordRequiredNameRule("tiered roster record needs a value"),
        ),
    )
}
