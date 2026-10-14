package com.acme.transport.lastmile.locker.lockerassignment

/** A rule a seasonal locker adjustment must pass. Check returns its problems, or none. */
interface SeasonalLockerAdjustmentRule {
    fun check(item: SeasonalLockerAdjustment): MutableList<String>
}

data class SeasonalLockerAdjustmentNotesLengthRule(var maxLength: Int) : SeasonalLockerAdjustmentRule {
    override fun check(item: SeasonalLockerAdjustment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerAdjustmentCodeLengthRule(var maxLength: Int) : SeasonalLockerAdjustmentRule {
    override fun check(item: SeasonalLockerAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerAdjustmentOdometerRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalLockerAdjustmentRule {
    override fun check(item: SeasonalLockerAdjustment): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class SeasonalLockerAdjustmentLabelLengthRule(var maxLength: Int) : SeasonalLockerAdjustmentRule {
    override fun check(item: SeasonalLockerAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal locker adjustment. */
data class SeasonalLockerAdjustmentValidator(var rules: MutableList<SeasonalLockerAdjustmentRule>) {
    fun validate(item: SeasonalLockerAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalLockerAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalLockerAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalLockerAdjustmentValidator(): SeasonalLockerAdjustmentValidator {
    return SeasonalLockerAdjustmentValidator(
        mutableListOf<SeasonalLockerAdjustmentRule>(
            SeasonalLockerAdjustmentNotesLengthRule(255),
            SeasonalLockerAdjustmentCodeLengthRule(32),
            SeasonalLockerAdjustmentOdometerRangeRule(64, 80),
            SeasonalLockerAdjustmentLabelLengthRule(1000),
        ),
    )
}
