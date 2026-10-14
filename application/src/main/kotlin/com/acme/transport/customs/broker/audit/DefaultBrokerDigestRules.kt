package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.text.normalize

/** A rule a default broker digest must pass. Check returns its problems, or none. */
interface DefaultBrokerDigestRule {
    fun check(item: DefaultBrokerDigest): MutableList<String>
}

data class DefaultBrokerDigestAllowedCodeRule(var allowed: MutableList<String>) : DefaultBrokerDigestRule {
    override fun check(item: DefaultBrokerDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DefaultBrokerDigestRequiredRouteCodeRule(var message: String) : DefaultBrokerDigestRule {
    override fun check(item: DefaultBrokerDigest): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultBrokerDigestTitleLengthRule(var maxLength: Int) : DefaultBrokerDigestRule {
    override fun check(item: DefaultBrokerDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default broker digest. */
data class DefaultBrokerDigestValidator(var rules: MutableList<DefaultBrokerDigestRule>) {
    fun validate(item: DefaultBrokerDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultBrokerDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultBrokerDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultBrokerDigestValidator(): DefaultBrokerDigestValidator {
    return DefaultBrokerDigestValidator(
        mutableListOf<DefaultBrokerDigestRule>(
            DefaultBrokerDigestAllowedCodeRule(mutableListOf<String>()),
            DefaultBrokerDigestRequiredRouteCodeRule("default broker digest needs a value"),
            DefaultBrokerDigestTitleLengthRule(1000),
        ),
    )
}
