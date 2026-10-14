package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.text.normalize

/** A rule a tiered broker request must pass. Check returns its problems, or none. */
interface TieredBrokerRequestRule {
    fun check(item: TieredBrokerRequest): MutableList<String>
}

data class TieredBrokerRequestAllowedCodeRule(var allowed: MutableList<String>) : TieredBrokerRequestRule {
    override fun check(item: TieredBrokerRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerRequestPhoneLengthRule(var maxLength: Int) : TieredBrokerRequestRule {
    override fun check(item: TieredBrokerRequest): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerRequestCodeLengthRule(var maxLength: Int) : TieredBrokerRequestRule {
    override fun check(item: TieredBrokerRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered broker request. */
data class TieredBrokerRequestValidator(var rules: MutableList<TieredBrokerRequestRule>) {
    fun validate(item: TieredBrokerRequest): MutableList<String> {
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

    fun requireValid(item: TieredBrokerRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredBrokerRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredBrokerRequestValidator(): TieredBrokerRequestValidator {
    return TieredBrokerRequestValidator(
        mutableListOf<TieredBrokerRequestRule>(
            TieredBrokerRequestAllowedCodeRule(mutableListOf<String>("scheduled")),
            TieredBrokerRequestPhoneLengthRule(255),
            TieredBrokerRequestCodeLengthRule(1000),
        ),
    )
}
