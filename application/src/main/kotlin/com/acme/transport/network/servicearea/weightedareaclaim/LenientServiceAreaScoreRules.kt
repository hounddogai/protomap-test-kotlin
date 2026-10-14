package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.kit.text.normalize

/** A rule a lenient service area score must pass. Check returns its problems, or none. */
interface LenientServiceAreaScoreRule {
    fun check(item: LenientServiceAreaScore): MutableList<String>
}

data class LenientServiceAreaScoreAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : LenientServiceAreaScoreRule {
    override fun check(item: LenientServiceAreaScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaScoreRequiredCodeRule(var message: String) : LenientServiceAreaScoreRule {
    override fun check(item: LenientServiceAreaScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaScoreQuantityRangeRule(var min: Int, var max: Int) : LenientServiceAreaScoreRule {
    override fun check(item: LenientServiceAreaScore): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaScoreCodePrefixRule(var prefix: String) : LenientServiceAreaScoreRule {
    override fun check(item: LenientServiceAreaScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaScoreTitleLengthRule(var maxLength: Int) : LenientServiceAreaScoreRule {
    override fun check(item: LenientServiceAreaScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient service area score. */
data class LenientServiceAreaScoreValidator(var rules: MutableList<LenientServiceAreaScoreRule>) {
    fun validate(item: LenientServiceAreaScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientServiceAreaScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServiceAreaScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServiceAreaScoreValidator(): LenientServiceAreaScoreValidator {
    return LenientServiceAreaScoreValidator(
        mutableListOf<LenientServiceAreaScoreRule>(
            LenientServiceAreaScoreAllowedOwnerIdRule(mutableListOf<String>()),
            LenientServiceAreaScoreRequiredCodeRule("lenient service area score needs this field"),
            LenientServiceAreaScoreQuantityRangeRule(80, 3600),
            LenientServiceAreaScoreCodePrefixRule("X"),
            LenientServiceAreaScoreTitleLengthRule(86400),
        ),
    )
}
