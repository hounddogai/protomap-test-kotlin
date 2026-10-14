package com.acme.transport.lastmile.redelivery.manifest

/** A rule a legacy redelivery policy must pass. Check returns its problems, or none. */
interface LegacyRedeliveryPolicyRule {
    fun check(item: LegacyRedeliveryPolicy): MutableList<String>
}

data class LegacyRedeliveryPolicyGraceDaysRangeRule(var min: Int, var max: Int) : LegacyRedeliveryPolicyRule {
    override fun check(item: LegacyRedeliveryPolicy): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class LegacyRedeliveryPolicyRequiredLabelRule(var message: String) : LegacyRedeliveryPolicyRule {
    override fun check(item: LegacyRedeliveryPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRedeliveryPolicyThresholdRangeRule(
    var min: Double,
    var max: Double,
) : LegacyRedeliveryPolicyRule {
    override fun check(item: LegacyRedeliveryPolicy): MutableList<String> {
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

/** Runs every rule of a legacy redelivery policy. */
data class LegacyRedeliveryPolicyValidator(var rules: MutableList<LegacyRedeliveryPolicyRule>) {
    fun validate(item: LegacyRedeliveryPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyRedeliveryPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRedeliveryPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRedeliveryPolicyValidator(): LegacyRedeliveryPolicyValidator {
    return LegacyRedeliveryPolicyValidator(
        mutableListOf<LegacyRedeliveryPolicyRule>(
            LegacyRedeliveryPolicyGraceDaysRangeRule(64, 255),
            LegacyRedeliveryPolicyRequiredLabelRule("legacy redelivery policy needs a value"),
            LegacyRedeliveryPolicyThresholdRangeRule(7.1, 1396.5),
        ),
    )
}
