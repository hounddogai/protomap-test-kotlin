package com.acme.transport.emissions.emissionfactor.target

/** A rule a emission factor target must pass. Check returns its problems, or none. */
interface EmissionFactorTargetRule {
    fun check(item: EmissionFactorTarget): MutableList<String>
}

data class EmissionFactorTargetContactEmailLengthRule(var maxLength: Int) : EmissionFactorTargetRule {
    override fun check(item: EmissionFactorTarget): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorTargetRequiredCodeRule(var message: String) : EmissionFactorTargetRule {
    override fun check(item: EmissionFactorTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorTargetCodeLengthRule(var maxLength: Int) : EmissionFactorTargetRule {
    override fun check(item: EmissionFactorTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorTargetTitleLengthRule(var maxLength: Int) : EmissionFactorTargetRule {
    override fun check(item: EmissionFactorTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor target. */
data class EmissionFactorTargetValidator(var rules: MutableList<EmissionFactorTargetRule>) {
    fun validate(item: EmissionFactorTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorTargetValidator(): EmissionFactorTargetValidator {
    return EmissionFactorTargetValidator(
        mutableListOf<EmissionFactorTargetRule>(
            EmissionFactorTargetContactEmailLengthRule(86400),
            EmissionFactorTargetRequiredCodeRule("emission factor target needs a value"),
            EmissionFactorTargetCodeLengthRule(255),
            EmissionFactorTargetTitleLengthRule(16),
        ),
    )
}
