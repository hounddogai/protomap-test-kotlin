package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.normalize

/** A rule a broker template must pass. Check returns its problems, or none. */
interface BrokerTemplateRule {
    fun check(item: BrokerTemplate): MutableList<String>
}

data class BrokerTemplateRequiredLabelRule(var message: String) : BrokerTemplateRule {
    override fun check(item: BrokerTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerTemplateCodePrefixRule(var prefix: String) : BrokerTemplateRule {
    override fun check(item: BrokerTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerTemplateAllowedCodeRule(var allowed: MutableList<String>) : BrokerTemplateRule {
    override fun check(item: BrokerTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class BrokerTemplateCodeLengthRule(var maxLength: Int) : BrokerTemplateRule {
    override fun check(item: BrokerTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerTemplateRequiredContactEmailRule(var message: String) : BrokerTemplateRule {
    override fun check(item: BrokerTemplate): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerTemplateOdometerRangeRule(var min: Int, var max: Int) : BrokerTemplateRule {
    override fun check(item: BrokerTemplate): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a broker template. */
data class BrokerTemplateValidator(var rules: MutableList<BrokerTemplateRule>) {
    fun validate(item: BrokerTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerTemplateValidator(): BrokerTemplateValidator {
    return BrokerTemplateValidator(
        mutableListOf<BrokerTemplateRule>(
            BrokerTemplateRequiredLabelRule("broker template needs a value"),
            BrokerTemplateCodePrefixRule("X"),
            BrokerTemplateAllowedCodeRule(mutableListOf<String>()),
            BrokerTemplateCodeLengthRule(1000),
            BrokerTemplateRequiredContactEmailRule("broker template needs this field"),
            BrokerTemplateOdometerRangeRule(16, 255),
        ),
    )
}
