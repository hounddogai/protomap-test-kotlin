package com.acme.transport.fleetops.tire.entry

/** A rule a tire record must pass. Check returns its problems, or none. */
interface TireRecordRule {
    fun check(item: TireRecord): MutableList<String>
}

data class TireRecordFuelLitersRangeRule(var min: Double, var max: Double) : TireRecordRule {
    override fun check(item: TireRecord): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class TireRecordTitleLengthRule(var maxLength: Int) : TireRecordRule {
    override fun check(item: TireRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireRecordContactEmailLengthRule(var maxLength: Int) : TireRecordRule {
    override fun check(item: TireRecord): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire record. */
data class TireRecordValidator(var rules: MutableList<TireRecordRule>) {
    fun validate(item: TireRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultTireRecordValidator(): TireRecordValidator {
    return TireRecordValidator(
        mutableListOf<TireRecordRule>(
            TireRecordFuelLitersRangeRule(9.4, 4862.3),
            TireRecordTitleLengthRule(80),
            TireRecordContactEmailLengthRule(32),
        ),
    )
}
