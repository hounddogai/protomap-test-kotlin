package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.text.normalize

/** A rule a declaration estimate must pass. Check returns its problems, or none. */
interface DeclarationEstimateRule {
    fun check(item: DeclarationEstimate): MutableList<String>
}

data class DeclarationEstimateAllowedCodeRule(var allowed: MutableList<String>) : DeclarationEstimateRule {
    override fun check(item: DeclarationEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationEstimateVisibleGuardRule(var limit: Int) : DeclarationEstimateRule {
    override fun check(item: DeclarationEstimate): MutableList<String> {
        if (item.visible && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationEstimateRequiredTitleRule(var message: String) : DeclarationEstimateRule {
    override fun check(item: DeclarationEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationEstimateCodePrefixRule(var prefix: String) : DeclarationEstimateRule {
    override fun check(item: DeclarationEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationEstimateCodeLengthRule(var maxLength: Int) : DeclarationEstimateRule {
    override fun check(item: DeclarationEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationEstimateTitleLengthRule(var maxLength: Int) : DeclarationEstimateRule {
    override fun check(item: DeclarationEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration estimate. */
data class DeclarationEstimateValidator(var rules: MutableList<DeclarationEstimateRule>) {
    fun validate(item: DeclarationEstimate): MutableList<String> {
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

    fun requireValid(item: DeclarationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationEstimateValidator(): DeclarationEstimateValidator {
    return DeclarationEstimateValidator(
        mutableListOf<DeclarationEstimateRule>(
            DeclarationEstimateAllowedCodeRule(mutableListOf<String>("default", "primary")),
            DeclarationEstimateVisibleGuardRule(120),
            DeclarationEstimateRequiredTitleRule("declaration estimate needs a value"),
            DeclarationEstimateCodePrefixRule("X"),
            DeclarationEstimateCodeLengthRule(1000),
            DeclarationEstimateTitleLengthRule(64),
        ),
    )
}
