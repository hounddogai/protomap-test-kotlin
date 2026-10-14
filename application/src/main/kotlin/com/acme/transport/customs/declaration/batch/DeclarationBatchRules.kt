package com.acme.transport.customs.declaration.batch

import com.acme.transport.kit.text.normalize

/** A rule a declaration batch must pass. Check returns its problems, or none. */
interface DeclarationBatchRule {
    fun check(item: DeclarationBatch): MutableList<String>
}

data class DeclarationBatchAllowedTitleRule(var allowed: MutableList<String>) : DeclarationBatchRule {
    override fun check(item: DeclarationBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationBatchCodePrefixRule(var prefix: String) : DeclarationBatchRule {
    override fun check(item: DeclarationBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationBatchRequiredTitleRule(var message: String) : DeclarationBatchRule {
    override fun check(item: DeclarationBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationBatchCodeLengthRule(var maxLength: Int) : DeclarationBatchRule {
    override fun check(item: DeclarationBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationBatchPalletsRangeRule(var min: Int, var max: Int) : DeclarationBatchRule {
    override fun check(item: DeclarationBatch): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a declaration batch. */
data class DeclarationBatchValidator(var rules: MutableList<DeclarationBatchRule>) {
    fun validate(item: DeclarationBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationBatchValidator(): DeclarationBatchValidator {
    return DeclarationBatchValidator(
        mutableListOf<DeclarationBatchRule>(
            DeclarationBatchAllowedTitleRule(mutableListOf<String>("manual")),
            DeclarationBatchCodePrefixRule("ACM"),
            DeclarationBatchRequiredTitleRule("declaration batch needs a value"),
            DeclarationBatchCodeLengthRule(120),
            DeclarationBatchPalletsRangeRule(120, 255),
        ),
    )
}
