package com.acme.transport.fleetops.tire.tirewindow

/** A rule a rolling tire cycle must pass. Check returns its problems, or none. */
interface RollingTireCycleRule {
    fun check(item: RollingTireCycle): MutableList<String>
}

data class RollingTireCycleFloorValueRangeRule(var min: Double, var max: Double) : RollingTireCycleRule {
    override fun check(item: RollingTireCycle): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class RollingTireCycleRequiredTitleRule(var message: String) : RollingTireCycleRule {
    override fun check(item: RollingTireCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTireCycleCodePrefixRule(var prefix: String) : RollingTireCycleRule {
    override fun check(item: RollingTireCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingTireCyclePinnedGuardRule(var limit: Int) : RollingTireCycleRule {
    override fun check(item: RollingTireCycle): MutableList<String> {
        if (item.pinned && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tire cycle. */
data class RollingTireCycleValidator(var rules: MutableList<RollingTireCycleRule>) {
    fun validate(item: RollingTireCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingTireCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTireCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTireCycleValidator(): RollingTireCycleValidator {
    return RollingTireCycleValidator(
        mutableListOf<RollingTireCycleRule>(
            RollingTireCycleFloorValueRangeRule(3.3, 578.4),
            RollingTireCycleRequiredTitleRule("rolling tire cycle needs this field"),
            RollingTireCycleCodePrefixRule("X"),
            RollingTireCyclePinnedGuardRule(32),
        ),
    )
}
