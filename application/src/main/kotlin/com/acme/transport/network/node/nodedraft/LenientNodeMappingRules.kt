package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.normalize

/** A rule a lenient node mapping must pass. Check returns its problems, or none. */
interface LenientNodeMappingRule {
    fun check(item: LenientNodeMapping): MutableList<String>
}

data class LenientNodeMappingRetriesRangeRule(var min: Int, var max: Int) : LenientNodeMappingRule {
    override fun check(item: LenientNodeMapping): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeMappingAllowedCodeRule(var allowed: MutableList<String>) : LenientNodeMappingRule {
    override fun check(item: LenientNodeMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeMappingCodePrefixRule(var prefix: String) : LenientNodeMappingRule {
    override fun check(item: LenientNodeMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient node mapping. */
data class LenientNodeMappingValidator(var rules: MutableList<LenientNodeMappingRule>) {
    fun validate(item: LenientNodeMapping): MutableList<String> {
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

    fun requireValid(item: LenientNodeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientNodeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientNodeMappingValidator(): LenientNodeMappingValidator {
    return LenientNodeMappingValidator(
        mutableListOf<LenientNodeMappingRule>(
            LenientNodeMappingRetriesRangeRule(255, 255),
            LenientNodeMappingAllowedCodeRule(mutableListOf<String>("rolling", "default", "strict")),
            LenientNodeMappingCodePrefixRule("OPS"),
        ),
    )
}
