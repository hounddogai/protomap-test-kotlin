package com.acme.transport.network.zone.cycle

/** A rule a zone cycle must pass. Check returns its problems, or none. */
interface ZoneCycleRule {
    fun check(item: ZoneCycle): MutableList<String>
}

data class ZoneCycleRequiredDriverIdRule(var message: String) : ZoneCycleRule {
    override fun check(item: ZoneCycle): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneCycleUpdatedAtRangeRule(var min: Int, var max: Int) : ZoneCycleRule {
    override fun check(item: ZoneCycle): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneCycleRetriesRangeRule(var min: Int, var max: Int) : ZoneCycleRule {
    override fun check(item: ZoneCycle): MutableList<String> {
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

data class ZoneCycleRequiredCodeRule(var message: String) : ZoneCycleRule {
    override fun check(item: ZoneCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone cycle. */
data class ZoneCycleValidator(var rules: MutableList<ZoneCycleRule>) {
    fun validate(item: ZoneCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneCycleValidator(): ZoneCycleValidator {
    return ZoneCycleValidator(
        mutableListOf<ZoneCycleRule>(
            ZoneCycleRequiredDriverIdRule("zone cycle needs this field"),
            ZoneCycleUpdatedAtRangeRule(80, 3600),
            ZoneCycleRetriesRangeRule(64, 80),
            ZoneCycleRequiredCodeRule("zone cycle needs a value"),
        ),
    )
}
