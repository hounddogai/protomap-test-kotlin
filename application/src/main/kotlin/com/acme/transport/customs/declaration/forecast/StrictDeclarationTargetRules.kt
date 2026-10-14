package com.acme.transport.customs.declaration.forecast

/** A rule a strict declaration target must pass. Check returns its problems, or none. */
interface StrictDeclarationTargetRule {
    fun check(item: StrictDeclarationTarget): MutableList<String>
}

data class StrictDeclarationTargetWeightRangeRule(
    var min: Double,
    var max: Double,
) : StrictDeclarationTargetRule {
    override fun check(item: StrictDeclarationTarget): MutableList<String> {
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

data class StrictDeclarationTargetCodeLengthRule(var maxLength: Int) : StrictDeclarationTargetRule {
    override fun check(item: StrictDeclarationTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationTargetLabelLengthRule(var maxLength: Int) : StrictDeclarationTargetRule {
    override fun check(item: StrictDeclarationTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationTargetCountryLengthRule(var maxLength: Int) : StrictDeclarationTargetRule {
    override fun check(item: StrictDeclarationTarget): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict declaration target. */
data class StrictDeclarationTargetValidator(var rules: MutableList<StrictDeclarationTargetRule>) {
    fun validate(item: StrictDeclarationTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictDeclarationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDeclarationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDeclarationTargetValidator(): StrictDeclarationTargetValidator {
    return StrictDeclarationTargetValidator(
        mutableListOf<StrictDeclarationTargetRule>(
            StrictDeclarationTargetWeightRangeRule(5.6, 2607.4),
            StrictDeclarationTargetCodeLengthRule(80),
            StrictDeclarationTargetLabelLengthRule(255),
            StrictDeclarationTargetCountryLengthRule(32),
        ),
    )
}
