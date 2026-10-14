package com.acme.transport.customs.broker.bundle

/** A rule a capped broker variance must pass. Check returns its problems, or none. */
interface CappedBrokerVarianceRule {
    fun check(item: CappedBrokerVariance): MutableList<String>
}

data class CappedBrokerVarianceRequiredCodeRule(var message: String) : CappedBrokerVarianceRule {
    override fun check(item: CappedBrokerVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerVarianceCodePrefixRule(var prefix: String) : CappedBrokerVarianceRule {
    override fun check(item: CappedBrokerVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerVarianceCodeLengthRule(var maxLength: Int) : CappedBrokerVarianceRule {
    override fun check(item: CappedBrokerVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped broker variance. */
data class CappedBrokerVarianceValidator(var rules: MutableList<CappedBrokerVarianceRule>) {
    fun validate(item: CappedBrokerVariance): MutableList<String> {
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

    fun requireValid(item: CappedBrokerVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBrokerVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBrokerVarianceValidator(): CappedBrokerVarianceValidator {
    return CappedBrokerVarianceValidator(
        mutableListOf<CappedBrokerVarianceRule>(
            CappedBrokerVarianceRequiredCodeRule("capped broker variance needs a value"),
            CappedBrokerVarianceCodePrefixRule("Q"),
            CappedBrokerVarianceCodeLengthRule(86400),
        ),
    )
}
