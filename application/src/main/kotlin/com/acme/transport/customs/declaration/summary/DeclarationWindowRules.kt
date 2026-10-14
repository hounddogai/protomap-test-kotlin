package com.acme.transport.customs.declaration.summary

import com.acme.transport.kit.text.normalize

/** A rule a declaration window must pass. Check returns its problems, or none. */
interface DeclarationWindowRule {
    fun check(item: DeclarationWindow): MutableList<String>
}

data class DeclarationWindowRequiredCodeRule(var message: String) : DeclarationWindowRule {
    override fun check(item: DeclarationWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationWindowChecksumLengthRule(var maxLength: Int) : DeclarationWindowRule {
    override fun check(item: DeclarationWindow): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationWindowCodeLengthRule(var maxLength: Int) : DeclarationWindowRule {
    override fun check(item: DeclarationWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationWindowAllowedCodeRule(var allowed: MutableList<String>) : DeclarationWindowRule {
    override fun check(item: DeclarationWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration window. */
data class DeclarationWindowValidator(var rules: MutableList<DeclarationWindowRule>) {
    fun validate(item: DeclarationWindow): MutableList<String> {
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

    fun requireValid(item: DeclarationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationWindowValidator(): DeclarationWindowValidator {
    return DeclarationWindowValidator(
        mutableListOf<DeclarationWindowRule>(
            DeclarationWindowRequiredCodeRule("declaration window needs an entry"),
            DeclarationWindowChecksumLengthRule(1000),
            DeclarationWindowCodeLengthRule(255),
            DeclarationWindowAllowedCodeRule(mutableListOf<String>("manual")),
        ),
    )
}
