package com.acme.transport.linehaul.capacity.mapping

/** A rule a capacity mapping must pass. Check returns its problems, or none. */
interface CapacityMappingRule {
    fun check(item: CapacityMapping): MutableList<String>
}

data class CapacityMappingMinimumRangeRule(var min: Double, var max: Double) : CapacityMappingRule {
    override fun check(item: CapacityMapping): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class CapacityMappingRequiredUnitRule(var message: String) : CapacityMappingRule {
    override fun check(item: CapacityMapping): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityMappingUnitLengthRule(var maxLength: Int) : CapacityMappingRule {
    override fun check(item: CapacityMapping): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity mapping. */
data class CapacityMappingValidator(var rules: MutableList<CapacityMappingRule>) {
    fun validate(item: CapacityMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityMappingValidator(): CapacityMappingValidator {
    return CapacityMappingValidator(
        mutableListOf<CapacityMappingRule>(
            CapacityMappingMinimumRangeRule(2.5, 1812.8),
            CapacityMappingRequiredUnitRule("capacity mapping needs an entry"),
            CapacityMappingUnitLengthRule(64),
        ),
    )
}
