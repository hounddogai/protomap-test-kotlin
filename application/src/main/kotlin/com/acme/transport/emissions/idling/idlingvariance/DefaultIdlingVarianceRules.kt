package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.normalize

/** A rule a default idling variance must pass. Check returns its problems, or none. */
interface DefaultIdlingVarianceRule {
    fun check(item: DefaultIdlingVariance): MutableList<String>
}

data class DefaultIdlingVarianceNameLengthRule(var maxLength: Int) : DefaultIdlingVarianceRule {
    override fun check(item: DefaultIdlingVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingVarianceContactEmailLengthRule(var maxLength: Int) : DefaultIdlingVarianceRule {
    override fun check(item: DefaultIdlingVariance): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingVarianceAllowedCodeRule(var allowed: MutableList<String>) : DefaultIdlingVarianceRule {
    override fun check(item: DefaultIdlingVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingVarianceCodePrefixRule(var prefix: String) : DefaultIdlingVarianceRule {
    override fun check(item: DefaultIdlingVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingVarianceStopCountRangeRule(var min: Int, var max: Int) : DefaultIdlingVarianceRule {
    override fun check(item: DefaultIdlingVariance): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default idling variance. */
data class DefaultIdlingVarianceValidator(var rules: MutableList<DefaultIdlingVarianceRule>) {
    fun validate(item: DefaultIdlingVariance): MutableList<String> {
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

    fun requireValid(item: DefaultIdlingVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultIdlingVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultIdlingVarianceValidator(): DefaultIdlingVarianceValidator {
    return DefaultIdlingVarianceValidator(
        mutableListOf<DefaultIdlingVarianceRule>(
            DefaultIdlingVarianceNameLengthRule(255),
            DefaultIdlingVarianceContactEmailLengthRule(16),
            DefaultIdlingVarianceAllowedCodeRule(mutableListOf<String>("manual")),
            DefaultIdlingVarianceCodePrefixRule("REF"),
            DefaultIdlingVarianceStopCountRangeRule(32, 1000),
        ),
    )
}
