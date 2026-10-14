package com.acme.transport.customs.broker.batch

/** A rule a broker batch must pass. Check returns its problems, or none. */
interface BrokerBatchRule {
    fun check(item: BrokerBatch): MutableList<String>
}

data class BrokerBatchRequiredLabelRule(var message: String) : BrokerBatchRule {
    override fun check(item: BrokerBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerBatchPhoneLengthRule(var maxLength: Int) : BrokerBatchRule {
    override fun check(item: BrokerBatch): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerBatchCodePrefixRule(var prefix: String) : BrokerBatchRule {
    override fun check(item: BrokerBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerBatchRequiredPhoneRule(var message: String) : BrokerBatchRule {
    override fun check(item: BrokerBatch): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker batch. */
data class BrokerBatchValidator(var rules: MutableList<BrokerBatchRule>) {
    fun validate(item: BrokerBatch): MutableList<String> {
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

    fun requireValid(item: BrokerBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerBatchValidator(): BrokerBatchValidator {
    return BrokerBatchValidator(
        mutableListOf<BrokerBatchRule>(
            BrokerBatchRequiredLabelRule("broker batch needs a value"),
            BrokerBatchPhoneLengthRule(86400),
            BrokerBatchCodePrefixRule("REF"),
            BrokerBatchRequiredPhoneRule("broker batch needs an entry"),
        ),
    )
}
