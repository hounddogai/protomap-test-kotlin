package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.text.normalize

/** A rule a batched tariff digest must pass. Check returns its problems, or none. */
interface BatchedTariffDigestRule {
    fun check(item: BatchedTariffDigest): MutableList<String>
}

data class BatchedTariffDigestCodePrefixRule(var prefix: String) : BatchedTariffDigestRule {
    override fun check(item: BatchedTariffDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffDigestRequiredLocaleRule(var message: String) : BatchedTariffDigestRule {
    override fun check(item: BatchedTariffDigest): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffDigestAllowedCodeRule(var allowed: MutableList<String>) : BatchedTariffDigestRule {
    override fun check(item: BatchedTariffDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffDigestRequiredCarrierRule(var message: String) : BatchedTariffDigestRule {
    override fun check(item: BatchedTariffDigest): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffDigestLocaleLengthRule(var maxLength: Int) : BatchedTariffDigestRule {
    override fun check(item: BatchedTariffDigest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tariff digest. */
data class BatchedTariffDigestValidator(var rules: MutableList<BatchedTariffDigestRule>) {
    fun validate(item: BatchedTariffDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTariffDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTariffDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTariffDigestValidator(): BatchedTariffDigestValidator {
    return BatchedTariffDigestValidator(
        mutableListOf<BatchedTariffDigestRule>(
            BatchedTariffDigestCodePrefixRule("Q"),
            BatchedTariffDigestRequiredLocaleRule("batched tariff digest needs an entry"),
            BatchedTariffDigestAllowedCodeRule(mutableListOf<String>("pending", "strict")),
            BatchedTariffDigestRequiredCarrierRule("batched tariff digest needs an entry"),
            BatchedTariffDigestLocaleLengthRule(1000),
        ),
    )
}
