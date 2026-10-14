package com.acme.transport.freightpay.dispute.rule

/** A rule a weighted dispute rule must pass. Check returns its problems, or none. */
interface WeightedDisputeRuleRule {
    fun check(item: WeightedDisputeRule): MutableList<String>
}

data class WeightedDisputeRuleRevisionRangeRule(var min: Int, var max: Int) : WeightedDisputeRuleRule {
    override fun check(item: WeightedDisputeRule): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class WeightedDisputeRuleRequiredTrailerIdRule(var message: String) : WeightedDisputeRuleRule {
    override fun check(item: WeightedDisputeRule): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeRuleArchivedGuardRule(var limit: Double) : WeightedDisputeRuleRule {
    override fun check(item: WeightedDisputeRule): MutableList<String> {
        if (item.archived && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeRuleCodeLengthRule(var maxLength: Int) : WeightedDisputeRuleRule {
    override fun check(item: WeightedDisputeRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted dispute rule. */
data class WeightedDisputeRuleValidator(var rules: MutableList<WeightedDisputeRuleRule>) {
    fun validate(item: WeightedDisputeRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedDisputeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDisputeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDisputeRuleValidator(): WeightedDisputeRuleValidator {
    return WeightedDisputeRuleValidator(
        mutableListOf<WeightedDisputeRuleRule>(
            WeightedDisputeRuleRevisionRangeRule(80, 3600),
            WeightedDisputeRuleRequiredTrailerIdRule("weighted dispute rule needs a value"),
            WeightedDisputeRuleArchivedGuardRule(2.6),
            WeightedDisputeRuleCodeLengthRule(32),
        ),
    )
}
