package com.acme.transport.lastmile.parcel.allocation

/** A rule a strict parcel cycle must pass. Check returns its problems, or none. */
interface StrictParcelCycleRule {
    fun check(item: StrictParcelCycle): MutableList<String>
}

data class StrictParcelCycleCapacityRangeRule(var min: Int, var max: Int) : StrictParcelCycleRule {
    override fun check(item: StrictParcelCycle): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class StrictParcelCycleCodeLengthRule(var maxLength: Int) : StrictParcelCycleRule {
    override fun check(item: StrictParcelCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictParcelCycleReviewerLengthRule(var maxLength: Int) : StrictParcelCycleRule {
    override fun check(item: StrictParcelCycle): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictParcelCycleNameLengthRule(var maxLength: Int) : StrictParcelCycleRule {
    override fun check(item: StrictParcelCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictParcelCycleRequiredDriverIdRule(var message: String) : StrictParcelCycleRule {
    override fun check(item: StrictParcelCycle): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict parcel cycle. */
data class StrictParcelCycleValidator(var rules: MutableList<StrictParcelCycleRule>) {
    fun validate(item: StrictParcelCycle): MutableList<String> {
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

    fun requireValid(item: StrictParcelCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictParcelCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictParcelCycleValidator(): StrictParcelCycleValidator {
    return StrictParcelCycleValidator(
        mutableListOf<StrictParcelCycleRule>(
            StrictParcelCycleCapacityRangeRule(3600, 86400),
            StrictParcelCycleCodeLengthRule(64),
            StrictParcelCycleReviewerLengthRule(80),
            StrictParcelCycleNameLengthRule(80),
            StrictParcelCycleRequiredDriverIdRule("strict parcel cycle needs this field"),
        ),
    )
}
