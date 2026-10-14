package com.acme.transport.customs.clearance.clearancemilestone

/** A rule a clearance quota must pass. Check returns its problems, or none. */
interface ClearanceQuotaRule {
    fun check(item: ClearanceQuota): MutableList<String>
}

data class ClearanceQuotaRateRangeRule(var min: Double, var max: Double) : ClearanceQuotaRule {
    override fun check(item: ClearanceQuota): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceQuotaRequiredCodeRule(var message: String) : ClearanceQuotaRule {
    override fun check(item: ClearanceQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceQuotaDurationMinutesRangeRule(var min: Int, var max: Int) : ClearanceQuotaRule {
    override fun check(item: ClearanceQuota): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class ClearanceQuotaDescriptionLengthRule(var maxLength: Int) : ClearanceQuotaRule {
    override fun check(item: ClearanceQuota): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance quota. */
data class ClearanceQuotaValidator(var rules: MutableList<ClearanceQuotaRule>) {
    fun validate(item: ClearanceQuota): MutableList<String> {
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

    fun requireValid(item: ClearanceQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceQuotaValidator(): ClearanceQuotaValidator {
    return ClearanceQuotaValidator(
        mutableListOf<ClearanceQuotaRule>(
            ClearanceQuotaRateRangeRule(6.8, 245.5),
            ClearanceQuotaRequiredCodeRule("clearance quota needs a value"),
            ClearanceQuotaDurationMinutesRangeRule(255, 86400),
            ClearanceQuotaDescriptionLengthRule(3600),
        ),
    )
}
