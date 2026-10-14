package com.acme.transport.customs.declaration.baseline

import com.acme.transport.kit.text.normalize

/** A rule a declaration baseline must pass. Check returns its problems, or none. */
interface DeclarationBaselineRule {
    fun check(item: DeclarationBaseline): MutableList<String>
}

data class DeclarationBaselineRequiredNameRule(var message: String) : DeclarationBaselineRule {
    override fun check(item: DeclarationBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationBaselineShiftCodeLengthRule(var maxLength: Int) : DeclarationBaselineRule {
    override fun check(item: DeclarationBaseline): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationBaselineAllowedCodeRule(var allowed: MutableList<String>) : DeclarationBaselineRule {
    override fun check(item: DeclarationBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration baseline. */
data class DeclarationBaselineValidator(var rules: MutableList<DeclarationBaselineRule>) {
    fun validate(item: DeclarationBaseline): MutableList<String> {
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

    fun requireValid(item: DeclarationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationBaselineValidator(): DeclarationBaselineValidator {
    return DeclarationBaselineValidator(
        mutableListOf<DeclarationBaselineRule>(
            DeclarationBaselineRequiredNameRule("declaration baseline needs an entry"),
            DeclarationBaselineShiftCodeLengthRule(120),
            DeclarationBaselineAllowedCodeRule(mutableListOf<String>("fallback", "scheduled")),
        ),
    )
}
