package com.acme.transport.lastmile.redelivery.baseline

/** A rule a redelivery baseline must pass. Check returns its problems, or none. */
interface RedeliveryBaselineRule {
    fun check(item: RedeliveryBaseline): MutableList<String>
}

data class RedeliveryBaselineRequiredVehicleIdRule(var message: String) : RedeliveryBaselineRule {
    override fun check(item: RedeliveryBaseline): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryBaselineNameLengthRule(var maxLength: Int) : RedeliveryBaselineRule {
    override fun check(item: RedeliveryBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryBaselineWeightRangeRule(var min: Double, var max: Double) : RedeliveryBaselineRule {
    override fun check(item: RedeliveryBaseline): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class RedeliveryBaselineRequiredNameRule(var message: String) : RedeliveryBaselineRule {
    override fun check(item: RedeliveryBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryBaselineCodePrefixRule(var prefix: String) : RedeliveryBaselineRule {
    override fun check(item: RedeliveryBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery baseline. */
data class RedeliveryBaselineValidator(var rules: MutableList<RedeliveryBaselineRule>) {
    fun validate(item: RedeliveryBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryBaselineValidator(): RedeliveryBaselineValidator {
    return RedeliveryBaselineValidator(
        mutableListOf<RedeliveryBaselineRule>(
            RedeliveryBaselineRequiredVehicleIdRule("redelivery baseline needs this field"),
            RedeliveryBaselineNameLengthRule(16),
            RedeliveryBaselineWeightRangeRule(7.2, 2650.2),
            RedeliveryBaselineRequiredNameRule("redelivery baseline needs a value"),
            RedeliveryBaselineCodePrefixRule("X"),
        ),
    )
}
