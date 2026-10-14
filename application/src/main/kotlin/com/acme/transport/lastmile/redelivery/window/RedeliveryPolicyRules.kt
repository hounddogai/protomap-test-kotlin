package com.acme.transport.lastmile.redelivery.window

/** A rule a redelivery policy must pass. Check returns its problems, or none. */
interface RedeliveryPolicyRule {
    fun check(item: RedeliveryPolicy): MutableList<String>
}

data class RedeliveryPolicyStartsAtRangeRule(var min: Int, var max: Int) : RedeliveryPolicyRule {
    override fun check(item: RedeliveryPolicy): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryPolicyCodeLengthRule(var maxLength: Int) : RedeliveryPolicyRule {
    override fun check(item: RedeliveryPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryPolicyRequiredDriverIdRule(var message: String) : RedeliveryPolicyRule {
    override fun check(item: RedeliveryPolicy): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryPolicyTitleLengthRule(var maxLength: Int) : RedeliveryPolicyRule {
    override fun check(item: RedeliveryPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery policy. */
data class RedeliveryPolicyValidator(var rules: MutableList<RedeliveryPolicyRule>) {
    fun validate(item: RedeliveryPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryPolicyValidator(): RedeliveryPolicyValidator {
    return RedeliveryPolicyValidator(
        mutableListOf<RedeliveryPolicyRule>(
            RedeliveryPolicyStartsAtRangeRule(3600, 86400),
            RedeliveryPolicyCodeLengthRule(3600),
            RedeliveryPolicyRequiredDriverIdRule("redelivery policy needs an entry"),
            RedeliveryPolicyTitleLengthRule(1000),
        ),
    )
}
