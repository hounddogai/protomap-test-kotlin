package com.acme.transport.fleetops.tire.request

/** A rule a tiered tire entry must pass. Check returns its problems, or none. */
interface TieredTireEntryRule {
    fun check(item: TieredTireEntry): MutableList<String>
}

data class TieredTireEntryShiftCodeLengthRule(var maxLength: Int) : TieredTireEntryRule {
    override fun check(item: TieredTireEntry): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireEntryTitleLengthRule(var maxLength: Int) : TieredTireEntryRule {
    override fun check(item: TieredTireEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireEntrySequenceRangeRule(var min: Int, var max: Int) : TieredTireEntryRule {
    override fun check(item: TieredTireEntry): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireEntryRequiredCodeRule(var message: String) : TieredTireEntryRule {
    override fun check(item: TieredTireEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTireEntryPalletsRangeRule(var min: Int, var max: Int) : TieredTireEntryRule {
    override fun check(item: TieredTireEntry): MutableList<String> {
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

data class TieredTireEntryPriorityRangeRule(var min: Int, var max: Int) : TieredTireEntryRule {
    override fun check(item: TieredTireEntry): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered tire entry. */
data class TieredTireEntryValidator(var rules: MutableList<TieredTireEntryRule>) {
    fun validate(item: TieredTireEntry): MutableList<String> {
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

    fun requireValid(item: TieredTireEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTireEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTireEntryValidator(): TieredTireEntryValidator {
    return TieredTireEntryValidator(
        mutableListOf<TieredTireEntryRule>(
            TieredTireEntryShiftCodeLengthRule(32),
            TieredTireEntryTitleLengthRule(80),
            TieredTireEntrySequenceRangeRule(120, 3600),
            TieredTireEntryRequiredCodeRule("tiered tire entry needs this field"),
            TieredTireEntryPalletsRangeRule(64, 120),
            TieredTireEntryPriorityRangeRule(64, 255),
        ),
    )
}
