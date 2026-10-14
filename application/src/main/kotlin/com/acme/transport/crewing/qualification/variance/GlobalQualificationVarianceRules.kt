package com.acme.transport.crewing.qualification.variance

/** A rule a global qualification variance must pass. Check returns its problems, or none. */
interface GlobalQualificationVarianceRule {
    fun check(item: GlobalQualificationVariance): MutableList<String>
}

data class GlobalQualificationVarianceRateRangeRule(
    var min: Double,
    var max: Double,
) : GlobalQualificationVarianceRule {
    override fun check(item: GlobalQualificationVariance): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationVarianceCodeLengthRule(var maxLength: Int) : GlobalQualificationVarianceRule {
    override fun check(item: GlobalQualificationVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationVarianceDriverIdLengthRule(var maxLength: Int) : GlobalQualificationVarianceRule {
    override fun check(item: GlobalQualificationVariance): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationVarianceLabelLengthRule(var maxLength: Int) : GlobalQualificationVarianceRule {
    override fun check(item: GlobalQualificationVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global qualification variance. */
data class GlobalQualificationVarianceValidator(var rules: MutableList<GlobalQualificationVarianceRule>) {
    fun validate(item: GlobalQualificationVariance): MutableList<String> {
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

    fun requireValid(item: GlobalQualificationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalQualificationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalQualificationVarianceValidator(): GlobalQualificationVarianceValidator {
    return GlobalQualificationVarianceValidator(
        mutableListOf<GlobalQualificationVarianceRule>(
            GlobalQualificationVarianceRateRangeRule(7.5, 2492.6),
            GlobalQualificationVarianceCodeLengthRule(64),
            GlobalQualificationVarianceDriverIdLengthRule(80),
            GlobalQualificationVarianceLabelLengthRule(32),
        ),
    )
}
