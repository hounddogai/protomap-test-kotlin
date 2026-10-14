package com.acme.transport.fleetops.tire.estimate

/** A rule a tire mapping must pass. Check returns its problems, or none. */
interface TireMappingRule {
    fun check(item: TireMapping): MutableList<String>
}

data class TireMappingStopCountRangeRule(var min: Int, var max: Int) : TireMappingRule {
    override fun check(item: TireMapping): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireMappingRequiredCodeRule(var message: String) : TireMappingRule {
    override fun check(item: TireMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireMappingDriverIdLengthRule(var maxLength: Int) : TireMappingRule {
    override fun check(item: TireMapping): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire mapping. */
data class TireMappingValidator(var rules: MutableList<TireMappingRule>) {
    fun validate(item: TireMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultTireMappingValidator(): TireMappingValidator {
    return TireMappingValidator(
        mutableListOf<TireMappingRule>(
            TireMappingStopCountRangeRule(64, 120),
            TireMappingRequiredCodeRule("tire mapping needs this field"),
            TireMappingDriverIdLengthRule(3600),
        ),
    )
}
