package com.acme.transport.emissions.emissionfactor.factorschedule

/** A rule a default emission factor adjustment must pass. Check returns its problems, or none. */
interface DefaultEmissionFactorAdjustmentRule {
    fun check(item: DefaultEmissionFactorAdjustment): MutableList<String>
}

data class DefaultEmissionFactorAdjustmentCodeLengthRule(
    var maxLength: Int,
) : DefaultEmissionFactorAdjustmentRule {
    override fun check(item: DefaultEmissionFactorAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorAdjustmentCodePrefixRule(
    var prefix: String,
) : DefaultEmissionFactorAdjustmentRule {
    override fun check(item: DefaultEmissionFactorAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorAdjustmentVehicleIdLengthRule(
    var maxLength: Int,
) : DefaultEmissionFactorAdjustmentRule {
    override fun check(item: DefaultEmissionFactorAdjustment): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorAdjustmentMinimumRangeRule(
    var min: Double,
    var max: Double,
) : DefaultEmissionFactorAdjustmentRule {
    override fun check(item: DefaultEmissionFactorAdjustment): MutableList<String> {
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

/** Runs every rule of a default emission factor adjustment. */
data class DefaultEmissionFactorAdjustmentValidator(
    var rules: MutableList<DefaultEmissionFactorAdjustmentRule>,
) {
    fun validate(item: DefaultEmissionFactorAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultEmissionFactorAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEmissionFactorAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEmissionFactorAdjustmentValidator(): DefaultEmissionFactorAdjustmentValidator {
    return DefaultEmissionFactorAdjustmentValidator(
        mutableListOf<DefaultEmissionFactorAdjustmentRule>(
            DefaultEmissionFactorAdjustmentCodeLengthRule(16),
            DefaultEmissionFactorAdjustmentCodePrefixRule("ACM"),
            DefaultEmissionFactorAdjustmentVehicleIdLengthRule(3600),
            DefaultEmissionFactorAdjustmentMinimumRangeRule(2.4, 734.4),
        ),
    )
}
