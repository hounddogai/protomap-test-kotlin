package com.acme.transport.terminal.dwell.milestone

/** A rule a dwell record must pass. Check returns its problems, or none. */
interface DwellRecordRule {
    fun check(item: DwellRecord): MutableList<String>
}

data class DwellRecordTitleLengthRule(var maxLength: Int) : DwellRecordRule {
    override fun check(item: DwellRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellRecordCodePrefixRule(var prefix: String) : DwellRecordRule {
    override fun check(item: DwellRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DwellRecordCostRangeRule(var min: Double, var max: Double) : DwellRecordRule {
    override fun check(item: DwellRecord): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class DwellRecordRequiredVehicleIdRule(var message: String) : DwellRecordRule {
    override fun check(item: DwellRecord): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellRecordRequiredCodeRule(var message: String) : DwellRecordRule {
    override fun check(item: DwellRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell record. */
data class DwellRecordValidator(var rules: MutableList<DwellRecordRule>) {
    fun validate(item: DwellRecord): MutableList<String> {
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

    fun requireValid(item: DwellRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellRecordValidator(): DwellRecordValidator {
    return DwellRecordValidator(
        mutableListOf<DwellRecordRule>(
            DwellRecordTitleLengthRule(64),
            DwellRecordCodePrefixRule("REF"),
            DwellRecordCostRangeRule(3.2, 4639.0),
            DwellRecordRequiredVehicleIdRule("dwell record needs an entry"),
            DwellRecordRequiredCodeRule("dwell record needs this field"),
        ),
    )
}
