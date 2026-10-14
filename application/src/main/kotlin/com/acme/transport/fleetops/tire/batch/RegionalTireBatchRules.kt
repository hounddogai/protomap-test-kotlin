package com.acme.transport.fleetops.tire.batch

/** A rule a regional tire batch must pass. Check returns its problems, or none. */
interface RegionalTireBatchRule {
    fun check(item: RegionalTireBatch): MutableList<String>
}

data class RegionalTireBatchTitleLengthRule(var maxLength: Int) : RegionalTireBatchRule {
    override fun check(item: RegionalTireBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTireBatchWeightRangeRule(var min: Double, var max: Double) : RegionalTireBatchRule {
    override fun check(item: RegionalTireBatch): MutableList<String> {
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

data class RegionalTireBatchRequiredLabelRule(var message: String) : RegionalTireBatchRule {
    override fun check(item: RegionalTireBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional tire batch. */
data class RegionalTireBatchValidator(var rules: MutableList<RegionalTireBatchRule>) {
    fun validate(item: RegionalTireBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTireBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTireBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTireBatchValidator(): RegionalTireBatchValidator {
    return RegionalTireBatchValidator(
        mutableListOf<RegionalTireBatchRule>(
            RegionalTireBatchTitleLengthRule(64),
            RegionalTireBatchWeightRangeRule(4.1, 2691.0),
            RegionalTireBatchRequiredLabelRule("regional tire batch needs a value"),
        ),
    )
}
