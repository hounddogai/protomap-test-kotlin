package com.acme.transport.freightpay.dispute.metric

/** A rule a rolling dispute policy must pass. Check returns its problems, or none. */
interface RollingDisputePolicyRule {
    fun check(item: RollingDisputePolicy): MutableList<String>
}

data class RollingDisputePolicyCodeLengthRule(var maxLength: Int) : RollingDisputePolicyRule {
    override fun check(item: RollingDisputePolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputePolicyRequiredCountryRule(var message: String) : RollingDisputePolicyRule {
    override fun check(item: RollingDisputePolicy): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputePolicyRateRangeRule(var min: Double, var max: Double) : RollingDisputePolicyRule {
    override fun check(item: RollingDisputePolicy): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class RollingDisputePolicyRequiredCodeRule(var message: String) : RollingDisputePolicyRule {
    override fun check(item: RollingDisputePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dispute policy. */
data class RollingDisputePolicyValidator(var rules: MutableList<RollingDisputePolicyRule>) {
    fun validate(item: RollingDisputePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDisputePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDisputePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDisputePolicyValidator(): RollingDisputePolicyValidator {
    return RollingDisputePolicyValidator(
        mutableListOf<RollingDisputePolicyRule>(
            RollingDisputePolicyCodeLengthRule(32),
            RollingDisputePolicyRequiredCountryRule("rolling dispute policy needs this field"),
            RollingDisputePolicyRateRangeRule(4.1, 2682.5),
            RollingDisputePolicyRequiredCodeRule("rolling dispute policy needs a value"),
        ),
    )
}
