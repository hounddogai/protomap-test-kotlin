package com.acme.transport.emissions.emissionfactor.quota

/** A rule a fallback emission factor quota must pass. Check returns its problems, or none. */
interface FallbackEmissionFactorQuotaRule {
    fun check(item: FallbackEmissionFactorQuota): MutableList<String>
}

data class FallbackEmissionFactorQuotaVehicleIdLengthRule(
    var maxLength: Int,
) : FallbackEmissionFactorQuotaRule {
    override fun check(item: FallbackEmissionFactorQuota): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorQuotaNameLengthRule(var maxLength: Int) : FallbackEmissionFactorQuotaRule {
    override fun check(item: FallbackEmissionFactorQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorQuotaLocaleLengthRule(var maxLength: Int) : FallbackEmissionFactorQuotaRule {
    override fun check(item: FallbackEmissionFactorQuota): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorQuotaRequiredLocaleRule(
    var message: String,
) : FallbackEmissionFactorQuotaRule {
    override fun check(item: FallbackEmissionFactorQuota): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback emission factor quota. */
data class FallbackEmissionFactorQuotaValidator(var rules: MutableList<FallbackEmissionFactorQuotaRule>) {
    fun validate(item: FallbackEmissionFactorQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackEmissionFactorQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEmissionFactorQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEmissionFactorQuotaValidator(): FallbackEmissionFactorQuotaValidator {
    return FallbackEmissionFactorQuotaValidator(
        mutableListOf<FallbackEmissionFactorQuotaRule>(
            FallbackEmissionFactorQuotaVehicleIdLengthRule(32),
            FallbackEmissionFactorQuotaNameLengthRule(32),
            FallbackEmissionFactorQuotaLocaleLengthRule(16),
            FallbackEmissionFactorQuotaRequiredLocaleRule("fallback emission factor quota needs this field"),
        ),
    )
}
