package com.acme.transport.fleetops.serviceplan.recorditem

/** A rule a legacy service plan mapping must pass. Check returns its problems, or none. */
interface LegacyServicePlanMappingRule {
    fun check(item: LegacyServicePlanMapping): MutableList<String>
}

data class LegacyServicePlanMappingFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : LegacyServicePlanMappingRule {
    override fun check(item: LegacyServicePlanMapping): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanMappingEndsAtRangeRule(var min: Int, var max: Int) : LegacyServicePlanMappingRule {
    override fun check(item: LegacyServicePlanMapping): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanMappingSequenceRangeRule(
    var min: Int,
    var max: Int,
) : LegacyServicePlanMappingRule {
    override fun check(item: LegacyServicePlanMapping): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanMappingRouteCodeLengthRule(var maxLength: Int) : LegacyServicePlanMappingRule {
    override fun check(item: LegacyServicePlanMapping): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanMappingRequiredRouteCodeRule(var message: String) : LegacyServicePlanMappingRule {
    override fun check(item: LegacyServicePlanMapping): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanMappingCodeLengthRule(var maxLength: Int) : LegacyServicePlanMappingRule {
    override fun check(item: LegacyServicePlanMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy service plan mapping. */
data class LegacyServicePlanMappingValidator(var rules: MutableList<LegacyServicePlanMappingRule>) {
    fun validate(item: LegacyServicePlanMapping): MutableList<String> {
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

    fun requireValid(item: LegacyServicePlanMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyServicePlanMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyServicePlanMappingValidator(): LegacyServicePlanMappingValidator {
    return LegacyServicePlanMappingValidator(
        mutableListOf<LegacyServicePlanMappingRule>(
            LegacyServicePlanMappingFuelLitersRangeRule(8.7, 2996.8),
            LegacyServicePlanMappingEndsAtRangeRule(255, 255),
            LegacyServicePlanMappingSequenceRangeRule(120, 3600),
            LegacyServicePlanMappingRouteCodeLengthRule(255),
            LegacyServicePlanMappingRequiredRouteCodeRule("legacy service plan mapping needs an entry"),
            LegacyServicePlanMappingCodeLengthRule(3600),
        ),
    )
}
