package com.acme.transport.emissions.emissionfactor.factorpreference

/** A rule a adaptive emission factor assignment must pass. Check returns its problems, or none. */
interface AdaptiveEmissionFactorAssignmentRule {
    fun check(item: AdaptiveEmissionFactorAssignment): MutableList<String>
}

data class AdaptiveEmissionFactorAssignmentRatioRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveEmissionFactorAssignmentRule {
    override fun check(item: AdaptiveEmissionFactorAssignment): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorAssignmentSourceLengthRule(
    var maxLength: Int,
) : AdaptiveEmissionFactorAssignmentRule {
    override fun check(item: AdaptiveEmissionFactorAssignment): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorAssignmentCodePrefixRule(
    var prefix: String,
) : AdaptiveEmissionFactorAssignmentRule {
    override fun check(item: AdaptiveEmissionFactorAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorAssignmentCodeLengthRule(
    var maxLength: Int,
) : AdaptiveEmissionFactorAssignmentRule {
    override fun check(item: AdaptiveEmissionFactorAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorAssignmentRequiredCodeRule(
    var message: String,
) : AdaptiveEmissionFactorAssignmentRule {
    override fun check(item: AdaptiveEmissionFactorAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorAssignmentPriorityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveEmissionFactorAssignmentRule {
    override fun check(item: AdaptiveEmissionFactorAssignment): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive emission factor assignment. */
data class AdaptiveEmissionFactorAssignmentValidator(
    var rules: MutableList<AdaptiveEmissionFactorAssignmentRule>,
) {
    fun validate(item: AdaptiveEmissionFactorAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveEmissionFactorAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEmissionFactorAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEmissionFactorAssignmentValidator(): AdaptiveEmissionFactorAssignmentValidator {
    return AdaptiveEmissionFactorAssignmentValidator(
        mutableListOf<AdaptiveEmissionFactorAssignmentRule>(
            AdaptiveEmissionFactorAssignmentRatioRangeRule(4.2, 98.2),
            AdaptiveEmissionFactorAssignmentSourceLengthRule(32),
            AdaptiveEmissionFactorAssignmentCodePrefixRule("ACM"),
            AdaptiveEmissionFactorAssignmentCodeLengthRule(1000),
            AdaptiveEmissionFactorAssignmentRequiredCodeRule(
                "adaptive emission factor assignment needs a value",
            ),
            AdaptiveEmissionFactorAssignmentPriorityRangeRule(64, 255),
        ),
    )
}
