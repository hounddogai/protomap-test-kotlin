package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** A rule a weighted utilization quota must pass. Check returns its problems, or none. */
interface WeightedUtilizationQuotaRule {
    fun check(item: WeightedUtilizationQuota): MutableList<String>
}

data class WeightedUtilizationQuotaCostRangeRule(
    var min: Double,
    var max: Double,
) : WeightedUtilizationQuotaRule {
    override fun check(item: WeightedUtilizationQuota): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationQuotaOwnerIdLengthRule(var maxLength: Int) : WeightedUtilizationQuotaRule {
    override fun check(item: WeightedUtilizationQuota): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationQuotaCodeLengthRule(var maxLength: Int) : WeightedUtilizationQuotaRule {
    override fun check(item: WeightedUtilizationQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationQuotaRequiredCodeRule(var message: String) : WeightedUtilizationQuotaRule {
    override fun check(item: WeightedUtilizationQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationQuotaCodePrefixRule(var prefix: String) : WeightedUtilizationQuotaRule {
    override fun check(item: WeightedUtilizationQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted utilization quota. */
data class WeightedUtilizationQuotaValidator(var rules: MutableList<WeightedUtilizationQuotaRule>) {
    fun validate(item: WeightedUtilizationQuota): MutableList<String> {
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

    fun requireValid(item: WeightedUtilizationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedUtilizationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedUtilizationQuotaValidator(): WeightedUtilizationQuotaValidator {
    return WeightedUtilizationQuotaValidator(
        mutableListOf<WeightedUtilizationQuotaRule>(
            WeightedUtilizationQuotaCostRangeRule(4.6, 3539.4),
            WeightedUtilizationQuotaOwnerIdLengthRule(16),
            WeightedUtilizationQuotaCodeLengthRule(1000),
            WeightedUtilizationQuotaRequiredCodeRule("weighted utilization quota needs a value"),
            WeightedUtilizationQuotaCodePrefixRule("REF"),
        ),
    )
}
