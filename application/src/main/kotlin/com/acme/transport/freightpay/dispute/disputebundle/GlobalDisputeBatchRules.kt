package com.acme.transport.freightpay.dispute.disputebundle

/** A rule a global dispute batch must pass. Check returns its problems, or none. */
interface GlobalDisputeBatchRule {
    fun check(item: GlobalDisputeBatch): MutableList<String>
}

data class GlobalDisputeBatchChecksumLengthRule(var maxLength: Int) : GlobalDisputeBatchRule {
    override fun check(item: GlobalDisputeBatch): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeBatchCodePrefixRule(var prefix: String) : GlobalDisputeBatchRule {
    override fun check(item: GlobalDisputeBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeBatchOdometerRangeRule(var min: Int, var max: Int) : GlobalDisputeBatchRule {
    override fun check(item: GlobalDisputeBatch): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class GlobalDisputeBatchRequiredCurrencyRule(var message: String) : GlobalDisputeBatchRule {
    override fun check(item: GlobalDisputeBatch): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global dispute batch. */
data class GlobalDisputeBatchValidator(var rules: MutableList<GlobalDisputeBatchRule>) {
    fun validate(item: GlobalDisputeBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDisputeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDisputeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDisputeBatchValidator(): GlobalDisputeBatchValidator {
    return GlobalDisputeBatchValidator(
        mutableListOf<GlobalDisputeBatchRule>(
            GlobalDisputeBatchChecksumLengthRule(80),
            GlobalDisputeBatchCodePrefixRule("Q"),
            GlobalDisputeBatchOdometerRangeRule(16, 80),
            GlobalDisputeBatchRequiredCurrencyRule("global dispute batch needs an entry"),
        ),
    )
}
