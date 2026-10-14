package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.text.normalize

/** A rule a declaration profile must pass. Check returns its problems, or none. */
interface DeclarationProfileRule {
    fun check(item: DeclarationProfile): MutableList<String>
}

data class DeclarationProfileCodePrefixRule(var prefix: String) : DeclarationProfileRule {
    override fun check(item: DeclarationProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationProfileLoadKgRangeRule(var min: Double, var max: Double) : DeclarationProfileRule {
    override fun check(item: DeclarationProfile): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationProfileRequiredCodeRule(var message: String) : DeclarationProfileRule {
    override fun check(item: DeclarationProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationProfileRequiredNameRule(var message: String) : DeclarationProfileRule {
    override fun check(item: DeclarationProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationProfileAllowedCodeRule(var allowed: MutableList<String>) : DeclarationProfileRule {
    override fun check(item: DeclarationProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration profile. */
data class DeclarationProfileValidator(var rules: MutableList<DeclarationProfileRule>) {
    fun validate(item: DeclarationProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationProfileValidator(): DeclarationProfileValidator {
    return DeclarationProfileValidator(
        mutableListOf<DeclarationProfileRule>(
            DeclarationProfileCodePrefixRule("REF"),
            DeclarationProfileLoadKgRangeRule(5.3, 4563.5),
            DeclarationProfileRequiredCodeRule("declaration profile needs an entry"),
            DeclarationProfileRequiredNameRule("declaration profile needs an entry"),
            DeclarationProfileAllowedCodeRule(mutableListOf<String>("regional")),
        ),
    )
}
