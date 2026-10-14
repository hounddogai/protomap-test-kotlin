package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.text.normalize

/** A rule a seasonal swap exception must pass. Check returns its problems, or none. */
interface SeasonalSwapExceptionRule {
    fun check(item: SeasonalSwapException): MutableList<String>
}

data class SeasonalSwapExceptionCodeLengthRule(var maxLength: Int) : SeasonalSwapExceptionRule {
    override fun check(item: SeasonalSwapException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSwapExceptionRequiredCodeRule(var message: String) : SeasonalSwapExceptionRule {
    override fun check(item: SeasonalSwapException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSwapExceptionAllowedChecksumRule(
    var allowed: MutableList<String>,
) : SeasonalSwapExceptionRule {
    override fun check(item: SeasonalSwapException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal swap exception. */
data class SeasonalSwapExceptionValidator(var rules: MutableList<SeasonalSwapExceptionRule>) {
    fun validate(item: SeasonalSwapException): MutableList<String> {
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

    fun requireValid(item: SeasonalSwapException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalSwapException>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalSwapExceptionValidator(): SeasonalSwapExceptionValidator {
    return SeasonalSwapExceptionValidator(
        mutableListOf<SeasonalSwapExceptionRule>(
            SeasonalSwapExceptionCodeLengthRule(1000),
            SeasonalSwapExceptionRequiredCodeRule("seasonal swap exception needs an entry"),
            SeasonalSwapExceptionAllowedChecksumRule(mutableListOf<String>("primary")),
        ),
    )
}
