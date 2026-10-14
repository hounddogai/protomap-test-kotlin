package com.acme.transport.safety.nearmiss.window

/** A rule a near miss quota must pass. Check returns its problems, or none. */
interface NearMissQuotaRule {
    fun check(item: NearMissQuota): MutableList<String>
}

data class NearMissQuotaRequiredLabelRule(var message: String) : NearMissQuotaRule {
    override fun check(item: NearMissQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissQuotaThresholdRangeRule(var min: Double, var max: Double) : NearMissQuotaRule {
    override fun check(item: NearMissQuota): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class NearMissQuotaRequiredCodeRule(var message: String) : NearMissQuotaRule {
    override fun check(item: NearMissQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss quota. */
data class NearMissQuotaValidator(var rules: MutableList<NearMissQuotaRule>) {
    fun validate(item: NearMissQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissQuotaValidator(): NearMissQuotaValidator {
    return NearMissQuotaValidator(
        mutableListOf<NearMissQuotaRule>(
            NearMissQuotaRequiredLabelRule("near miss quota needs an entry"),
            NearMissQuotaThresholdRangeRule(0.9, 3691.8),
            NearMissQuotaRequiredCodeRule("near miss quota needs this field"),
        ),
    )
}
