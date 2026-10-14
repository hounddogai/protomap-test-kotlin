package com.acme.transport.fleetops.serviceplan.quota

/** A rule a lenient service plan quota must pass. Check returns its problems, or none. */
interface LenientServicePlanQuotaRule {
    fun check(item: LenientServicePlanQuota): MutableList<String>
}

data class LenientServicePlanQuotaLabelLengthRule(var maxLength: Int) : LenientServicePlanQuotaRule {
    override fun check(item: LenientServicePlanQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientServicePlanQuotaCodeLengthRule(var maxLength: Int) : LenientServicePlanQuotaRule {
    override fun check(item: LenientServicePlanQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientServicePlanQuotaCodePrefixRule(var prefix: String) : LenientServicePlanQuotaRule {
    override fun check(item: LenientServicePlanQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientServicePlanQuotaRequiredLabelRule(var message: String) : LenientServicePlanQuotaRule {
    override fun check(item: LenientServicePlanQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient service plan quota. */
data class LenientServicePlanQuotaValidator(var rules: MutableList<LenientServicePlanQuotaRule>) {
    fun validate(item: LenientServicePlanQuota): MutableList<String> {
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

    fun requireValid(item: LenientServicePlanQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServicePlanQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServicePlanQuotaValidator(): LenientServicePlanQuotaValidator {
    return LenientServicePlanQuotaValidator(
        mutableListOf<LenientServicePlanQuotaRule>(
            LenientServicePlanQuotaLabelLengthRule(80),
            LenientServicePlanQuotaCodeLengthRule(16),
            LenientServicePlanQuotaCodePrefixRule("Q"),
            LenientServicePlanQuotaRequiredLabelRule("lenient service plan quota needs this field"),
        ),
    )
}
