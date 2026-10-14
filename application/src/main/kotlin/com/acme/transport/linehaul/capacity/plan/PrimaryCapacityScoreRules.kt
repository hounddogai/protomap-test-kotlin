package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.text.normalize

/** A rule a primary capacity score must pass. Check returns its problems, or none. */
interface PrimaryCapacityScoreRule {
    fun check(item: PrimaryCapacityScore): MutableList<String>
}

data class PrimaryCapacityScoreRequiredCountryRule(var message: String) : PrimaryCapacityScoreRule {
    override fun check(item: PrimaryCapacityScore): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityScoreShiftCodeLengthRule(var maxLength: Int) : PrimaryCapacityScoreRule {
    override fun check(item: PrimaryCapacityScore): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityScoreNameLengthRule(var maxLength: Int) : PrimaryCapacityScoreRule {
    override fun check(item: PrimaryCapacityScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityScoreAllowedCodeRule(var allowed: MutableList<String>) : PrimaryCapacityScoreRule {
    override fun check(item: PrimaryCapacityScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityScoreCodePrefixRule(var prefix: String) : PrimaryCapacityScoreRule {
    override fun check(item: PrimaryCapacityScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityScoreRequiredNameRule(var message: String) : PrimaryCapacityScoreRule {
    override fun check(item: PrimaryCapacityScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary capacity score. */
data class PrimaryCapacityScoreValidator(var rules: MutableList<PrimaryCapacityScoreRule>) {
    fun validate(item: PrimaryCapacityScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryCapacityScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCapacityScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCapacityScoreValidator(): PrimaryCapacityScoreValidator {
    return PrimaryCapacityScoreValidator(
        mutableListOf<PrimaryCapacityScoreRule>(
            PrimaryCapacityScoreRequiredCountryRule("primary capacity score needs an entry"),
            PrimaryCapacityScoreShiftCodeLengthRule(1000),
            PrimaryCapacityScoreNameLengthRule(64),
            PrimaryCapacityScoreAllowedCodeRule(mutableListOf<String>("rolling", "strict")),
            PrimaryCapacityScoreCodePrefixRule("REF"),
            PrimaryCapacityScoreRequiredNameRule("primary capacity score needs a value"),
        ),
    )
}
