package com.acme.transport.customs.broker.request

import com.acme.transport.kit.text.normalize

/** A rule a pending broker score must pass. Check returns its problems, or none. */
interface PendingBrokerScoreRule {
    fun check(item: PendingBrokerScore): MutableList<String>
}

data class PendingBrokerScoreAllowedCodeRule(var allowed: MutableList<String>) : PendingBrokerScoreRule {
    override fun check(item: PendingBrokerScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerScoreRequiredExternalRefRule(var message: String) : PendingBrokerScoreRule {
    override fun check(item: PendingBrokerScore): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerScoreCodeLengthRule(var maxLength: Int) : PendingBrokerScoreRule {
    override fun check(item: PendingBrokerScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerScoreRequiredCategoryRule(var message: String) : PendingBrokerScoreRule {
    override fun check(item: PendingBrokerScore): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerScoreRequiredNameRule(var message: String) : PendingBrokerScoreRule {
    override fun check(item: PendingBrokerScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending broker score. */
data class PendingBrokerScoreValidator(var rules: MutableList<PendingBrokerScoreRule>) {
    fun validate(item: PendingBrokerScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingBrokerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingBrokerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingBrokerScoreValidator(): PendingBrokerScoreValidator {
    return PendingBrokerScoreValidator(
        mutableListOf<PendingBrokerScoreRule>(
            PendingBrokerScoreAllowedCodeRule(mutableListOf<String>("manual", "tiered")),
            PendingBrokerScoreRequiredExternalRefRule("pending broker score needs an entry"),
            PendingBrokerScoreCodeLengthRule(16),
            PendingBrokerScoreRequiredCategoryRule("pending broker score needs a value"),
            PendingBrokerScoreRequiredNameRule("pending broker score needs an entry"),
        ),
    )
}
