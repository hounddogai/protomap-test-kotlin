package com.acme.transport.safety.violation.manualviolationquota

/** A rule a strict violation quota must pass. Check returns its problems, or none. */
interface StrictViolationQuotaRule {
    fun check(item: StrictViolationQuota): MutableList<String>
}

data class StrictViolationQuotaMarginRangeRule(var min: Double, var max: Double) : StrictViolationQuotaRule {
    override fun check(item: StrictViolationQuota): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationQuotaRequiredCodeRule(var message: String) : StrictViolationQuotaRule {
    override fun check(item: StrictViolationQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationQuotaRequiredTitleRule(var message: String) : StrictViolationQuotaRule {
    override fun check(item: StrictViolationQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationQuotaWeightRangeRule(var min: Double, var max: Double) : StrictViolationQuotaRule {
    override fun check(item: StrictViolationQuota): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict violation quota. */
data class StrictViolationQuotaValidator(var rules: MutableList<StrictViolationQuotaRule>) {
    fun validate(item: StrictViolationQuota): MutableList<String> {
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

    fun requireValid(item: StrictViolationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictViolationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictViolationQuotaValidator(): StrictViolationQuotaValidator {
    return StrictViolationQuotaValidator(
        mutableListOf<StrictViolationQuotaRule>(
            StrictViolationQuotaMarginRangeRule(6.9, 828.7),
            StrictViolationQuotaRequiredCodeRule("strict violation quota needs this field"),
            StrictViolationQuotaRequiredTitleRule("strict violation quota needs this field"),
            StrictViolationQuotaWeightRangeRule(4.1, 3861.4),
        ),
    )
}
