package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.text.normalize

/** A rule a legacy trailer yard exception must pass. Check returns its problems, or none. */
interface LegacyTrailerYardExceptionRule {
    fun check(item: LegacyTrailerYardException): MutableList<String>
}

data class LegacyTrailerYardExceptionRouteCodeLengthRule(var maxLength: Int) : LegacyTrailerYardExceptionRule {
    override fun check(item: LegacyTrailerYardException): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardExceptionRequiredRouteCodeRule(
    var message: String,
) : LegacyTrailerYardExceptionRule {
    override fun check(item: LegacyTrailerYardException): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardExceptionAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : LegacyTrailerYardExceptionRule {
    override fun check(item: LegacyTrailerYardException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy trailer yard exception. */
data class LegacyTrailerYardExceptionValidator(var rules: MutableList<LegacyTrailerYardExceptionRule>) {
    fun validate(item: LegacyTrailerYardException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTrailerYardException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTrailerYardException>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTrailerYardExceptionValidator(): LegacyTrailerYardExceptionValidator {
    return LegacyTrailerYardExceptionValidator(
        mutableListOf<LegacyTrailerYardExceptionRule>(
            LegacyTrailerYardExceptionRouteCodeLengthRule(120),
            LegacyTrailerYardExceptionRequiredRouteCodeRule("legacy trailer yard exception needs an entry"),
            LegacyTrailerYardExceptionAllowedRouteCodeRule(mutableListOf<String>()),
        ),
    )
}
