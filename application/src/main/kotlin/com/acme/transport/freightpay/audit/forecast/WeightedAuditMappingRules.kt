package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.kit.text.normalize

/** A rule a weighted audit mapping must pass. Check returns its problems, or none. */
interface WeightedAuditMappingRule {
    fun check(item: WeightedAuditMapping): MutableList<String>
}

data class WeightedAuditMappingShiftCodeLengthRule(var maxLength: Int) : WeightedAuditMappingRule {
    override fun check(item: WeightedAuditMapping): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditMappingCodeLengthRule(var maxLength: Int) : WeightedAuditMappingRule {
    override fun check(item: WeightedAuditMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditMappingAllowedCodeRule(var allowed: MutableList<String>) : WeightedAuditMappingRule {
    override fun check(item: WeightedAuditMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditMappingCodePrefixRule(var prefix: String) : WeightedAuditMappingRule {
    override fun check(item: WeightedAuditMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditMappingTitleLengthRule(var maxLength: Int) : WeightedAuditMappingRule {
    override fun check(item: WeightedAuditMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted audit mapping. */
data class WeightedAuditMappingValidator(var rules: MutableList<WeightedAuditMappingRule>) {
    fun validate(item: WeightedAuditMapping): MutableList<String> {
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

    fun requireValid(item: WeightedAuditMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAuditMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAuditMappingValidator(): WeightedAuditMappingValidator {
    return WeightedAuditMappingValidator(
        mutableListOf<WeightedAuditMappingRule>(
            WeightedAuditMappingShiftCodeLengthRule(120),
            WeightedAuditMappingCodeLengthRule(255),
            WeightedAuditMappingAllowedCodeRule(mutableListOf<String>()),
            WeightedAuditMappingCodePrefixRule("ACM"),
            WeightedAuditMappingTitleLengthRule(120),
        ),
    )
}
