package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.text.normalize

/** A rule a adaptive audit mapping must pass. Check returns its problems, or none. */
interface AdaptiveAuditMappingRule {
    fun check(item: AdaptiveAuditMapping): MutableList<String>
}

data class AdaptiveAuditMappingCurrencyLengthRule(var maxLength: Int) : AdaptiveAuditMappingRule {
    override fun check(item: AdaptiveAuditMapping): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditMappingCodePrefixRule(var prefix: String) : AdaptiveAuditMappingRule {
    override fun check(item: AdaptiveAuditMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditMappingAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : AdaptiveAuditMappingRule {
    override fun check(item: AdaptiveAuditMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditMappingLabelLengthRule(var maxLength: Int) : AdaptiveAuditMappingRule {
    override fun check(item: AdaptiveAuditMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditMappingOwnerIdLengthRule(var maxLength: Int) : AdaptiveAuditMappingRule {
    override fun check(item: AdaptiveAuditMapping): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditMappingCodeLengthRule(var maxLength: Int) : AdaptiveAuditMappingRule {
    override fun check(item: AdaptiveAuditMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive audit mapping. */
data class AdaptiveAuditMappingValidator(var rules: MutableList<AdaptiveAuditMappingRule>) {
    fun validate(item: AdaptiveAuditMapping): MutableList<String> {
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

    fun requireValid(item: AdaptiveAuditMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAuditMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAuditMappingValidator(): AdaptiveAuditMappingValidator {
    return AdaptiveAuditMappingValidator(
        mutableListOf<AdaptiveAuditMappingRule>(
            AdaptiveAuditMappingCurrencyLengthRule(80),
            AdaptiveAuditMappingCodePrefixRule("REF"),
            AdaptiveAuditMappingAllowedVehicleIdRule(mutableListOf<String>("scheduled", "rolling")),
            AdaptiveAuditMappingLabelLengthRule(3600),
            AdaptiveAuditMappingOwnerIdLengthRule(3600),
            AdaptiveAuditMappingCodeLengthRule(32),
        ),
    )
}
