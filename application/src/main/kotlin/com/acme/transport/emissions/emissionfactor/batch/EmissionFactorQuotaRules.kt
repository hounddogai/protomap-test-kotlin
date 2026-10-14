package com.acme.transport.emissions.emissionfactor.batch

/** A rule a emission factor quota must pass. Check returns its problems, or none. */
interface EmissionFactorQuotaRule {
    fun check(item: EmissionFactorQuota): MutableList<String>
}

data class EmissionFactorQuotaRequiredLaneRule(var message: String) : EmissionFactorQuotaRule {
    override fun check(item: EmissionFactorQuota): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorQuotaRequiredLabelRule(var message: String) : EmissionFactorQuotaRule {
    override fun check(item: EmissionFactorQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorQuotaCodePrefixRule(var prefix: String) : EmissionFactorQuotaRule {
    override fun check(item: EmissionFactorQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorQuotaLabelLengthRule(var maxLength: Int) : EmissionFactorQuotaRule {
    override fun check(item: EmissionFactorQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor quota. */
data class EmissionFactorQuotaValidator(var rules: MutableList<EmissionFactorQuotaRule>) {
    fun validate(item: EmissionFactorQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorQuotaValidator(): EmissionFactorQuotaValidator {
    return EmissionFactorQuotaValidator(
        mutableListOf<EmissionFactorQuotaRule>(
            EmissionFactorQuotaRequiredLaneRule("emission factor quota needs a value"),
            EmissionFactorQuotaRequiredLabelRule("emission factor quota needs a value"),
            EmissionFactorQuotaCodePrefixRule("ACM"),
            EmissionFactorQuotaLabelLengthRule(32),
        ),
    )
}
