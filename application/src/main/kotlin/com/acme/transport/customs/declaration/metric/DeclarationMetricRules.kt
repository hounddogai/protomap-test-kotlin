package com.acme.transport.customs.declaration.metric

import com.acme.transport.kit.text.normalize

/** A rule a declaration metric must pass. Check returns its problems, or none. */
interface DeclarationMetricRule {
    fun check(item: DeclarationMetric): MutableList<String>
}

data class DeclarationMetricAllowedCodeRule(var allowed: MutableList<String>) : DeclarationMetricRule {
    override fun check(item: DeclarationMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationMetricThresholdRangeRule(var min: Double, var max: Double) : DeclarationMetricRule {
    override fun check(item: DeclarationMetric): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class DeclarationMetricCodePrefixRule(var prefix: String) : DeclarationMetricRule {
    override fun check(item: DeclarationMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationMetricRequiredCodeRule(var message: String) : DeclarationMetricRule {
    override fun check(item: DeclarationMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationMetricCodeLengthRule(var maxLength: Int) : DeclarationMetricRule {
    override fun check(item: DeclarationMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration metric. */
data class DeclarationMetricValidator(var rules: MutableList<DeclarationMetricRule>) {
    fun validate(item: DeclarationMetric): MutableList<String> {
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

    fun requireValid(item: DeclarationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationMetricValidator(): DeclarationMetricValidator {
    return DeclarationMetricValidator(
        mutableListOf<DeclarationMetricRule>(
            DeclarationMetricAllowedCodeRule(mutableListOf<String>("lenient", "strict", "weighted")),
            DeclarationMetricThresholdRangeRule(7.4, 186.1),
            DeclarationMetricCodePrefixRule("Q"),
            DeclarationMetricRequiredCodeRule("declaration metric needs this field"),
            DeclarationMetricCodeLengthRule(120),
        ),
    )
}
