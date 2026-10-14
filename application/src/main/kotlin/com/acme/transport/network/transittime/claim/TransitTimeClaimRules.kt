package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.normalize

/** A rule a transit time claim must pass. Check returns its problems, or none. */
interface TransitTimeClaimRule {
    fun check(item: TransitTimeClaim): MutableList<String>
}

data class TransitTimeClaimNameLengthRule(var maxLength: Int) : TransitTimeClaimRule {
    override fun check(item: TransitTimeClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeClaimRequiredNameRule(var message: String) : TransitTimeClaimRule {
    override fun check(item: TransitTimeClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeClaimAllowedCodeRule(var allowed: MutableList<String>) : TransitTimeClaimRule {
    override fun check(item: TransitTimeClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeClaimRequiredCodeRule(var message: String) : TransitTimeClaimRule {
    override fun check(item: TransitTimeClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time claim. */
data class TransitTimeClaimValidator(var rules: MutableList<TransitTimeClaimRule>) {
    fun validate(item: TransitTimeClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeClaimValidator(): TransitTimeClaimValidator {
    return TransitTimeClaimValidator(
        mutableListOf<TransitTimeClaimRule>(
            TransitTimeClaimNameLengthRule(32),
            TransitTimeClaimRequiredNameRule("transit time claim needs a value"),
            TransitTimeClaimAllowedCodeRule(mutableListOf<String>("strict")),
            TransitTimeClaimRequiredCodeRule("transit time claim needs an entry"),
        ),
    )
}
