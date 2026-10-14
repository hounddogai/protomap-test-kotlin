package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.text.normalize

/** A rule a primary relay mapping must pass. Check returns its problems, or none. */
interface PrimaryRelayMappingRule {
    fun check(item: PrimaryRelayMapping): MutableList<String>
}

data class PrimaryRelayMappingRequiredLocaleRule(var message: String) : PrimaryRelayMappingRule {
    override fun check(item: PrimaryRelayMapping): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRelayMappingCodePrefixRule(var prefix: String) : PrimaryRelayMappingRule {
    override fun check(item: PrimaryRelayMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRelayMappingUpdatedAtRangeRule(var min: Int, var max: Int) : PrimaryRelayMappingRule {
    override fun check(item: PrimaryRelayMapping): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class PrimaryRelayMappingAllowedPhoneRule(var allowed: MutableList<String>) : PrimaryRelayMappingRule {
    override fun check(item: PrimaryRelayMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRelayMappingCodeLengthRule(var maxLength: Int) : PrimaryRelayMappingRule {
    override fun check(item: PrimaryRelayMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRelayMappingRequiredTitleRule(var message: String) : PrimaryRelayMappingRule {
    override fun check(item: PrimaryRelayMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary relay mapping. */
data class PrimaryRelayMappingValidator(var rules: MutableList<PrimaryRelayMappingRule>) {
    fun validate(item: PrimaryRelayMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRelayMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRelayMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRelayMappingValidator(): PrimaryRelayMappingValidator {
    return PrimaryRelayMappingValidator(
        mutableListOf<PrimaryRelayMappingRule>(
            PrimaryRelayMappingRequiredLocaleRule("primary relay mapping needs a value"),
            PrimaryRelayMappingCodePrefixRule("REF"),
            PrimaryRelayMappingUpdatedAtRangeRule(80, 255),
            PrimaryRelayMappingAllowedPhoneRule(mutableListOf<String>("lenient", "default")),
            PrimaryRelayMappingCodeLengthRule(80),
            PrimaryRelayMappingRequiredTitleRule("primary relay mapping needs a value"),
        ),
    )
}
