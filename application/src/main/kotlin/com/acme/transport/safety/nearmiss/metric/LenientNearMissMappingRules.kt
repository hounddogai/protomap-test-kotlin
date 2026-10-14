package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.text.normalize

/** A rule a lenient near miss mapping must pass. Check returns its problems, or none. */
interface LenientNearMissMappingRule {
    fun check(item: LenientNearMissMapping): MutableList<String>
}

data class LenientNearMissMappingCodeLengthRule(var maxLength: Int) : LenientNearMissMappingRule {
    override fun check(item: LenientNearMissMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissMappingCodePrefixRule(var prefix: String) : LenientNearMissMappingRule {
    override fun check(item: LenientNearMissMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissMappingAllowedNameRule(
    var allowed: MutableList<String>,
) : LenientNearMissMappingRule {
    override fun check(item: LenientNearMissMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissMappingCountryLengthRule(var maxLength: Int) : LenientNearMissMappingRule {
    override fun check(item: LenientNearMissMapping): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissMappingGraceDaysRangeRule(var min: Int, var max: Int) : LenientNearMissMappingRule {
    override fun check(item: LenientNearMissMapping): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient near miss mapping. */
data class LenientNearMissMappingValidator(var rules: MutableList<LenientNearMissMappingRule>) {
    fun validate(item: LenientNearMissMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientNearMissMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientNearMissMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientNearMissMappingValidator(): LenientNearMissMappingValidator {
    return LenientNearMissMappingValidator(
        mutableListOf<LenientNearMissMappingRule>(
            LenientNearMissMappingCodeLengthRule(120),
            LenientNearMissMappingCodePrefixRule("ACM"),
            LenientNearMissMappingAllowedNameRule(mutableListOf<String>("weighted")),
            LenientNearMissMappingCountryLengthRule(3600),
            LenientNearMissMappingGraceDaysRangeRule(64, 1000),
        ),
    )
}
