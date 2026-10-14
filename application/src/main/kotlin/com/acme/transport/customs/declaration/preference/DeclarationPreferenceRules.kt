package com.acme.transport.customs.declaration.preference

import com.acme.transport.kit.text.normalize

/** A rule a declaration preference must pass. Check returns its problems, or none. */
interface DeclarationPreferenceRule {
    fun check(item: DeclarationPreference): MutableList<String>
}

data class DeclarationPreferenceMinimumRangeRule(var min: Double, var max: Double) : DeclarationPreferenceRule {
    override fun check(item: DeclarationPreference): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DeclarationPreferenceRequiredCodeRule(var message: String) : DeclarationPreferenceRule {
    override fun check(item: DeclarationPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPreferenceCodePrefixRule(var prefix: String) : DeclarationPreferenceRule {
    override fun check(item: DeclarationPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPreferenceTitleLengthRule(var maxLength: Int) : DeclarationPreferenceRule {
    override fun check(item: DeclarationPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPreferenceAllowedCodeRule(var allowed: MutableList<String>) : DeclarationPreferenceRule {
    override fun check(item: DeclarationPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationPreferenceRequiredOwnerIdRule(var message: String) : DeclarationPreferenceRule {
    override fun check(item: DeclarationPreference): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration preference. */
data class DeclarationPreferenceValidator(var rules: MutableList<DeclarationPreferenceRule>) {
    fun validate(item: DeclarationPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationPreferenceValidator(): DeclarationPreferenceValidator {
    return DeclarationPreferenceValidator(
        mutableListOf<DeclarationPreferenceRule>(
            DeclarationPreferenceMinimumRangeRule(0.8, 2084.0),
            DeclarationPreferenceRequiredCodeRule("declaration preference needs an entry"),
            DeclarationPreferenceCodePrefixRule("Q"),
            DeclarationPreferenceTitleLengthRule(64),
            DeclarationPreferenceAllowedCodeRule(mutableListOf<String>("regional", "tiered")),
            DeclarationPreferenceRequiredOwnerIdRule("declaration preference needs this field"),
        ),
    )
}
