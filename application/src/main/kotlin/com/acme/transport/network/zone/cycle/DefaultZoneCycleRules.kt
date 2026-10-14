package com.acme.transport.network.zone.cycle

/** A rule a default zone cycle must pass. Check returns its problems, or none. */
interface DefaultZoneCycleRule {
    fun check(item: DefaultZoneCycle): MutableList<String>
}

data class DefaultZoneCycleExpiresAtRangeRule(var min: Int, var max: Int) : DefaultZoneCycleRule {
    override fun check(item: DefaultZoneCycle): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultZoneCycleCodeLengthRule(var maxLength: Int) : DefaultZoneCycleRule {
    override fun check(item: DefaultZoneCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultZoneCycleWindowDaysRangeRule(var min: Int, var max: Int) : DefaultZoneCycleRule {
    override fun check(item: DefaultZoneCycle): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultZoneCycleRequiredLaneRule(var message: String) : DefaultZoneCycleRule {
    override fun check(item: DefaultZoneCycle): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultZoneCycleLaneLengthRule(var maxLength: Int) : DefaultZoneCycleRule {
    override fun check(item: DefaultZoneCycle): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default zone cycle. */
data class DefaultZoneCycleValidatorExtra(var rules: MutableList<DefaultZoneCycleRule>) {
    fun validate(item: DefaultZoneCycle): MutableList<String> {
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

    fun requireValid(item: DefaultZoneCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultZoneCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultZoneCycleValidator(): DefaultZoneCycleValidatorExtra {
    return DefaultZoneCycleValidatorExtra(
        mutableListOf<DefaultZoneCycleRule>(
            DefaultZoneCycleExpiresAtRangeRule(120, 86400),
            DefaultZoneCycleCodeLengthRule(3600),
            DefaultZoneCycleWindowDaysRangeRule(80, 86400),
            DefaultZoneCycleRequiredLaneRule("default zone cycle needs an entry"),
            DefaultZoneCycleLaneLengthRule(32),
        ),
    )
}
