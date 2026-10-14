package com.acme.transport.safety.cameraevent.group

/** A rule a rolling camera event override must pass. Check returns its problems, or none. */
interface RollingCameraEventOverrideRule {
    fun check(item: RollingCameraEventOverride): MutableList<String>
}

data class RollingCameraEventOverrideRegionLengthRule(var maxLength: Int) : RollingCameraEventOverrideRule {
    override fun check(item: RollingCameraEventOverride): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCameraEventOverrideMarginRangeRule(
    var min: Double,
    var max: Double,
) : RollingCameraEventOverrideRule {
    override fun check(item: RollingCameraEventOverride): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingCameraEventOverrideRouteCodeLengthRule(var maxLength: Int) : RollingCameraEventOverrideRule {
    override fun check(item: RollingCameraEventOverride): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCameraEventOverrideCodeLengthRule(var maxLength: Int) : RollingCameraEventOverrideRule {
    override fun check(item: RollingCameraEventOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling camera event override. */
data class RollingCameraEventOverrideValidator(var rules: MutableList<RollingCameraEventOverrideRule>) {
    fun validate(item: RollingCameraEventOverride): MutableList<String> {
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

    fun requireValid(item: RollingCameraEventOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCameraEventOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCameraEventOverrideValidator(): RollingCameraEventOverrideValidator {
    return RollingCameraEventOverrideValidator(
        mutableListOf<RollingCameraEventOverrideRule>(
            RollingCameraEventOverrideRegionLengthRule(32),
            RollingCameraEventOverrideMarginRangeRule(8.9, 3083.0),
            RollingCameraEventOverrideRouteCodeLengthRule(86400),
            RollingCameraEventOverrideCodeLengthRule(1000),
        ),
    )
}
