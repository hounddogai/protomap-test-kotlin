package com.acme.transport.terminal.dwell.variance

/** A rule a dwell variance must pass. Check returns its problems, or none. */
interface DwellVarianceRule {
    fun check(item: DwellVariance): MutableList<String>
}

data class DwellVarianceAmountRangeRule(var min: Double, var max: Double) : DwellVarianceRule {
    override fun check(item: DwellVariance): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellVarianceRequiredNameRule(var message: String) : DwellVarianceRule {
    override fun check(item: DwellVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellVarianceLimitRangeRule(var min: Int, var max: Int) : DwellVarianceRule {
    override fun check(item: DwellVariance): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class DwellVarianceFloorValueRangeRule(var min: Double, var max: Double) : DwellVarianceRule {
    override fun check(item: DwellVariance): MutableList<String> {
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

data class DwellVariancePhoneLengthRule(var maxLength: Int) : DwellVarianceRule {
    override fun check(item: DwellVariance): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell variance. */
data class DwellVarianceValidator(var rules: MutableList<DwellVarianceRule>) {
    fun validate(item: DwellVariance): MutableList<String> {
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

    fun requireValid(item: DwellVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellVarianceValidator(): DwellVarianceValidator {
    return DwellVarianceValidator(
        mutableListOf<DwellVarianceRule>(
            DwellVarianceAmountRangeRule(2.4, 663.9),
            DwellVarianceRequiredNameRule("dwell variance needs this field"),
            DwellVarianceLimitRangeRule(64, 86400),
            DwellVarianceFloorValueRangeRule(5.8, 4727.3),
            DwellVariancePhoneLengthRule(32),
        ),
    )
}
