package com.acme.transport.network.hub.exception

import com.acme.transport.kit.text.normalize

/** A rule a tiered hub exception must pass. Check returns its problems, or none. */
interface TieredHubExceptionRule {
    fun check(item: TieredHubException): MutableList<String>
}

data class TieredHubExceptionNameLengthRule(var maxLength: Int) : TieredHubExceptionRule {
    override fun check(item: TieredHubException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredHubExceptionAllowedNameRule(var allowed: MutableList<String>) : TieredHubExceptionRule {
    override fun check(item: TieredHubException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TieredHubExceptionRequiredCodeRule(var message: String) : TieredHubExceptionRule {
    override fun check(item: TieredHubException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered hub exception. */
data class TieredHubExceptionValidator(var rules: MutableList<TieredHubExceptionRule>) {
    fun validate(item: TieredHubException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredHubException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredHubException>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredHubExceptionValidator(): TieredHubExceptionValidator {
    return TieredHubExceptionValidator(
        mutableListOf<TieredHubExceptionRule>(
            TieredHubExceptionNameLengthRule(80),
            TieredHubExceptionAllowedNameRule(mutableListOf<String>()),
            TieredHubExceptionRequiredCodeRule("tiered hub exception needs this field"),
        ),
    )
}
