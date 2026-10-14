package com.acme.transport.lastmile.locker.cycle

/** A rule a weighted locker cycle must pass. Check returns its problems, or none. */
interface WeightedLockerCycleRule {
    fun check(item: WeightedLockerCycle): MutableList<String>
}

data class WeightedLockerCycleOwnerIdLengthRule(var maxLength: Int) : WeightedLockerCycleRule {
    override fun check(item: WeightedLockerCycle): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLockerCycleFuelLitersRangeRule(var min: Double, var max: Double) : WeightedLockerCycleRule {
    override fun check(item: WeightedLockerCycle): MutableList<String> {
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

data class WeightedLockerCycleCodeLengthRule(var maxLength: Int) : WeightedLockerCycleRule {
    override fun check(item: WeightedLockerCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLockerCycleRequiredSegmentRule(var message: String) : WeightedLockerCycleRule {
    override fun check(item: WeightedLockerCycle): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted locker cycle. */
data class WeightedLockerCycleValidator(var rules: MutableList<WeightedLockerCycleRule>) {
    fun validate(item: WeightedLockerCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedLockerCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLockerCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLockerCycleValidator(): WeightedLockerCycleValidator {
    return WeightedLockerCycleValidator(
        mutableListOf<WeightedLockerCycleRule>(
            WeightedLockerCycleOwnerIdLengthRule(16),
            WeightedLockerCycleFuelLitersRangeRule(4.4, 191.1),
            WeightedLockerCycleCodeLengthRule(86400),
            WeightedLockerCycleRequiredSegmentRule("weighted locker cycle needs this field"),
        ),
    )
}
