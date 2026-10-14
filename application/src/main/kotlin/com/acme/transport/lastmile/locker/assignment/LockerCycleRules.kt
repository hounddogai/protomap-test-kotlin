package com.acme.transport.lastmile.locker.assignment

/** A rule a locker cycle must pass. Check returns its problems, or none. */
interface LockerCycleRule {
    fun check(item: LockerCycle): MutableList<String>
}

data class LockerCycleRequiredTitleRule(var message: String) : LockerCycleRule {
    override fun check(item: LockerCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerCycleCodeLengthRule(var maxLength: Int) : LockerCycleRule {
    override fun check(item: LockerCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerCycleStopCountRangeRule(var min: Int, var max: Int) : LockerCycleRule {
    override fun check(item: LockerCycle): MutableList<String> {
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

data class LockerCycleTitleLengthRule(var maxLength: Int) : LockerCycleRule {
    override fun check(item: LockerCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerCycleRequiredDockRule(var message: String) : LockerCycleRule {
    override fun check(item: LockerCycle): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker cycle. */
data class LockerCycleValidator(var rules: MutableList<LockerCycleRule>) {
    fun validate(item: LockerCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerCycleValidator(): LockerCycleValidator {
    return LockerCycleValidator(
        mutableListOf<LockerCycleRule>(
            LockerCycleRequiredTitleRule("locker cycle needs this field"),
            LockerCycleCodeLengthRule(80),
            LockerCycleStopCountRangeRule(32, 3600),
            LockerCycleTitleLengthRule(1000),
            LockerCycleRequiredDockRule("locker cycle needs this field"),
        ),
    )
}
