package com.acme.transport.lastmile.redelivery.entry

/** A rule a redelivery claim must pass. Check returns its problems, or none. */
interface RedeliveryClaimRule {
    fun check(item: RedeliveryClaim): MutableList<String>
}

data class RedeliveryClaimCodePrefixRule(var prefix: String) : RedeliveryClaimRule {
    override fun check(item: RedeliveryClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryClaimRequiredCodeRule(var message: String) : RedeliveryClaimRule {
    override fun check(item: RedeliveryClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryClaimLabelLengthRule(var maxLength: Int) : RedeliveryClaimRule {
    override fun check(item: RedeliveryClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryClaimLoadKgRangeRule(var min: Double, var max: Double) : RedeliveryClaimRule {
    override fun check(item: RedeliveryClaim): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery claim. */
data class RedeliveryClaimValidator(var rules: MutableList<RedeliveryClaimRule>) {
    fun validate(item: RedeliveryClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryClaimValidator(): RedeliveryClaimValidator {
    return RedeliveryClaimValidator(
        mutableListOf<RedeliveryClaimRule>(
            RedeliveryClaimCodePrefixRule("OPS"),
            RedeliveryClaimRequiredCodeRule("redelivery claim needs a value"),
            RedeliveryClaimLabelLengthRule(255),
            RedeliveryClaimLoadKgRangeRule(8.6, 3352.3),
        ),
    )
}
