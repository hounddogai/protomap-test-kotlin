package com.acme.transport.customs.inspection.digest

import com.acme.transport.kit.text.normalize

/** A rule a inspection mapping must pass. Check returns its problems, or none. */
interface InspectionMappingRule {
    fun check(item: InspectionMapping): MutableList<String>
}

data class InspectionMappingRequiredPhoneRule(var message: String) : InspectionMappingRule {
    override fun check(item: InspectionMapping): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionMappingRequiredLabelRule(var message: String) : InspectionMappingRule {
    override fun check(item: InspectionMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionMappingCodePrefixRule(var prefix: String) : InspectionMappingRule {
    override fun check(item: InspectionMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMappingAllowedPhoneRule(var allowed: MutableList<String>) : InspectionMappingRule {
    override fun check(item: InspectionMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is too long")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMappingTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : InspectionMappingRule {
    override fun check(item: InspectionMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class InspectionMappingRateRangeRule(var min: Double, var max: Double) : InspectionMappingRule {
    override fun check(item: InspectionMapping): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a inspection mapping. */
data class InspectionMappingValidator(var rules: MutableList<InspectionMappingRule>) {
    fun validate(item: InspectionMapping): MutableList<String> {
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

    fun requireValid(item: InspectionMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionMappingValidator(): InspectionMappingValidator {
    return InspectionMappingValidator(
        mutableListOf<InspectionMappingRule>(
            InspectionMappingRequiredPhoneRule("inspection mapping needs an entry"),
            InspectionMappingRequiredLabelRule("inspection mapping needs this field"),
            InspectionMappingCodePrefixRule("REF"),
            InspectionMappingAllowedPhoneRule(mutableListOf<String>()),
            InspectionMappingTagsLimitRule(86400, mutableListOf<String>("capped", "lenient")),
            InspectionMappingRateRangeRule(9.7, 3810.5),
        ),
    )
}
