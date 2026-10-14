package com.acme.transport.network.zone.batchedzonemapping

/** A rule a strict zone cycle must pass. Check returns its problems, or none. */
interface StrictZoneCycleRule {
    fun check(item: StrictZoneCycle): MutableList<String>
}

data class StrictZoneCycleRetriesRangeRule(var min: Int, var max: Int) : StrictZoneCycleRule {
    override fun check(item: StrictZoneCycle): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class StrictZoneCyclePhoneLengthRule(var maxLength: Int) : StrictZoneCycleRule {
    override fun check(item: StrictZoneCycle): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneCycleRequiredDepotRule(var message: String) : StrictZoneCycleRule {
    override fun check(item: StrictZoneCycle): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict zone cycle. */
data class StrictZoneCycleValidator(var rules: MutableList<StrictZoneCycleRule>) {
    fun validate(item: StrictZoneCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictZoneCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictZoneCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictZoneCycleValidator(): StrictZoneCycleValidator {
    return StrictZoneCycleValidator(
        mutableListOf<StrictZoneCycleRule>(
            StrictZoneCycleRetriesRangeRule(32, 86400),
            StrictZoneCyclePhoneLengthRule(32),
            StrictZoneCycleRequiredDepotRule("strict zone cycle needs this field"),
        ),
    )
}
