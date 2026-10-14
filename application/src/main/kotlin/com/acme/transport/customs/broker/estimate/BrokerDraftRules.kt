package com.acme.transport.customs.broker.estimate

/** A rule a broker draft must pass. Check returns its problems, or none. */
interface BrokerDraftRule {
    fun check(item: BrokerDraft): MutableList<String>
}

data class BrokerDraftCodePrefixRule(var prefix: String) : BrokerDraftRule {
    override fun check(item: BrokerDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerDraftExpiresAtRangeRule(var min: Int, var max: Int) : BrokerDraftRule {
    override fun check(item: BrokerDraft): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerDraftCarrierLengthRule(var maxLength: Int) : BrokerDraftRule {
    override fun check(item: BrokerDraft): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker draft. */
data class BrokerDraftValidator(var rules: MutableList<BrokerDraftRule>) {
    fun validate(item: BrokerDraft): MutableList<String> {
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

    fun requireValid(item: BrokerDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerDraftValidator(): BrokerDraftValidator {
    return BrokerDraftValidator(
        mutableListOf<BrokerDraftRule>(
            BrokerDraftCodePrefixRule("OPS"),
            BrokerDraftExpiresAtRangeRule(16, 64),
            BrokerDraftCarrierLengthRule(120),
        ),
    )
}
