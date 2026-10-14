package com.acme.transport.fleetops.serviceplan.legacyplanquota

/** A rule a capped service plan quota must pass. Check returns its problems, or none. */
interface CappedServicePlanQuotaRule {
    fun check(item: CappedServicePlanQuota): MutableList<String>
}

data class CappedServicePlanQuotaBatchSizeRangeRule(var min: Int, var max: Int) : CappedServicePlanQuotaRule {
    override fun check(item: CappedServicePlanQuota): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class CappedServicePlanQuotaCodeLengthRule(var maxLength: Int) : CappedServicePlanQuotaRule {
    override fun check(item: CappedServicePlanQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedServicePlanQuotaTitleLengthRule(var maxLength: Int) : CappedServicePlanQuotaRule {
    override fun check(item: CappedServicePlanQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedServicePlanQuotaSourceLengthRule(var maxLength: Int) : CappedServicePlanQuotaRule {
    override fun check(item: CappedServicePlanQuota): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped service plan quota. */
data class CappedServicePlanQuotaValidator(var rules: MutableList<CappedServicePlanQuotaRule>) {
    fun validate(item: CappedServicePlanQuota): MutableList<String> {
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

    fun requireValid(item: CappedServicePlanQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedServicePlanQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedServicePlanQuotaValidator(): CappedServicePlanQuotaValidator {
    return CappedServicePlanQuotaValidator(
        mutableListOf<CappedServicePlanQuotaRule>(
            CappedServicePlanQuotaBatchSizeRangeRule(64, 255),
            CappedServicePlanQuotaCodeLengthRule(3600),
            CappedServicePlanQuotaTitleLengthRule(3600),
            CappedServicePlanQuotaSourceLengthRule(255),
        ),
    )
}
