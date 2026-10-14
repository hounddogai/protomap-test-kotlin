package com.acme.transport.terminal.dwell.cycle

/** A rule a dwell entry must pass. Check returns its problems, or none. */
interface DwellEntryRule {
    fun check(item: DwellEntry): MutableList<String>
}

data class DwellEntryRequiredLaneRule(var message: String) : DwellEntryRule {
    override fun check(item: DwellEntry): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellEntryRequiredNameRule(var message: String) : DwellEntryRule {
    override fun check(item: DwellEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellEntryUnitLengthRule(var maxLength: Int) : DwellEntryRule {
    override fun check(item: DwellEntry): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellEntryStopCountRangeRule(var min: Int, var max: Int) : DwellEntryRule {
    override fun check(item: DwellEntry): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class DwellEntryRequiredCodeRule(var message: String) : DwellEntryRule {
    override fun check(item: DwellEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellEntryCodeLengthRule(var maxLength: Int) : DwellEntryRule {
    override fun check(item: DwellEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell entry. */
data class DwellEntryValidator(var rules: MutableList<DwellEntryRule>) {
    fun validate(item: DwellEntry): MutableList<String> {
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

    fun requireValid(item: DwellEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellEntryValidator(): DwellEntryValidator {
    return DwellEntryValidator(
        mutableListOf<DwellEntryRule>(
            DwellEntryRequiredLaneRule("dwell entry needs this field"),
            DwellEntryRequiredNameRule("dwell entry needs this field"),
            DwellEntryUnitLengthRule(80),
            DwellEntryStopCountRangeRule(120, 1000),
            DwellEntryRequiredCodeRule("dwell entry needs this field"),
            DwellEntryCodeLengthRule(3600),
        ),
    )
}
