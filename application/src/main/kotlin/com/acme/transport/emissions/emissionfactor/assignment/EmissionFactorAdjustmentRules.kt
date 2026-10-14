package com.acme.transport.emissions.emissionfactor.assignment

/** A rule a emission factor adjustment must pass. Check returns its problems, or none. */
interface EmissionFactorAdjustmentRule {
    fun check(item: EmissionFactorAdjustment): MutableList<String>
}

data class EmissionFactorAdjustmentExternalRefLengthRule(var maxLength: Int) : EmissionFactorAdjustmentRule {
    override fun check(item: EmissionFactorAdjustment): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAdjustmentCodeLengthRule(var maxLength: Int) : EmissionFactorAdjustmentRule {
    override fun check(item: EmissionFactorAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAdjustmentCodePrefixRule(var prefix: String) : EmissionFactorAdjustmentRule {
    override fun check(item: EmissionFactorAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAdjustmentRequiredSummaryRule(var message: String) : EmissionFactorAdjustmentRule {
    override fun check(item: EmissionFactorAdjustment): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAdjustmentRequiredLabelRule(var message: String) : EmissionFactorAdjustmentRule {
    override fun check(item: EmissionFactorAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor adjustment. */
data class EmissionFactorAdjustmentValidator(var rules: MutableList<EmissionFactorAdjustmentRule>) {
    fun validate(item: EmissionFactorAdjustment): MutableList<String> {
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

    fun requireValid(item: EmissionFactorAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorAdjustmentValidator(): EmissionFactorAdjustmentValidator {
    return EmissionFactorAdjustmentValidator(
        mutableListOf<EmissionFactorAdjustmentRule>(
            EmissionFactorAdjustmentExternalRefLengthRule(3600),
            EmissionFactorAdjustmentCodeLengthRule(32),
            EmissionFactorAdjustmentCodePrefixRule("ACM"),
            EmissionFactorAdjustmentRequiredSummaryRule("emission factor adjustment needs an entry"),
            EmissionFactorAdjustmentRequiredLabelRule("emission factor adjustment needs an entry"),
        ),
    )
}
