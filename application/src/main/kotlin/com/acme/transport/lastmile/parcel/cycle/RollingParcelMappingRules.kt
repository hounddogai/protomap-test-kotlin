package com.acme.transport.lastmile.parcel.cycle

/** A rule a rolling parcel mapping must pass. Check returns its problems, or none. */
interface RollingParcelMappingRule {
    fun check(item: RollingParcelMapping): MutableList<String>
}

data class RollingParcelMappingScoreRangeRule(var min: Double, var max: Double) : RollingParcelMappingRule {
    override fun check(item: RollingParcelMapping): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingParcelMappingTitleLengthRule(var maxLength: Int) : RollingParcelMappingRule {
    override fun check(item: RollingParcelMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingParcelMappingDriverIdLengthRule(var maxLength: Int) : RollingParcelMappingRule {
    override fun check(item: RollingParcelMapping): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingParcelMappingRouteCodeLengthRule(var maxLength: Int) : RollingParcelMappingRule {
    override fun check(item: RollingParcelMapping): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingParcelMappingRequiredDriverIdRule(var message: String) : RollingParcelMappingRule {
    override fun check(item: RollingParcelMapping): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling parcel mapping. */
data class RollingParcelMappingValidator(var rules: MutableList<RollingParcelMappingRule>) {
    fun validate(item: RollingParcelMapping): MutableList<String> {
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

    fun requireValid(item: RollingParcelMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingParcelMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingParcelMappingValidator(): RollingParcelMappingValidator {
    return RollingParcelMappingValidator(
        mutableListOf<RollingParcelMappingRule>(
            RollingParcelMappingScoreRangeRule(9.0, 4348.9),
            RollingParcelMappingTitleLengthRule(3600),
            RollingParcelMappingDriverIdLengthRule(64),
            RollingParcelMappingRouteCodeLengthRule(32),
            RollingParcelMappingRequiredDriverIdRule("rolling parcel mapping needs a value"),
        ),
    )
}
