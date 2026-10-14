package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.text.normalize

/** A rule a scheduled proof of delivery mapping must pass. Check returns its problems, or none. */
interface ScheduledProofOfDeliveryMappingRule {
    fun check(item: ScheduledProofOfDeliveryMapping): MutableList<String>
}

data class ScheduledProofOfDeliveryMappingAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledProofOfDeliveryMappingRule {
    override fun check(item: ScheduledProofOfDeliveryMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryMappingRequiredNotesRule(
    var message: String,
) : ScheduledProofOfDeliveryMappingRule {
    override fun check(item: ScheduledProofOfDeliveryMapping): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryMappingRequiredNameRule(
    var message: String,
) : ScheduledProofOfDeliveryMappingRule {
    override fun check(item: ScheduledProofOfDeliveryMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryMappingCountryLengthRule(
    var maxLength: Int,
) : ScheduledProofOfDeliveryMappingRule {
    override fun check(item: ScheduledProofOfDeliveryMapping): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryMappingNameLengthRule(
    var maxLength: Int,
) : ScheduledProofOfDeliveryMappingRule {
    override fun check(item: ScheduledProofOfDeliveryMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryMappingCodePrefixRule(
    var prefix: String,
) : ScheduledProofOfDeliveryMappingRule {
    override fun check(item: ScheduledProofOfDeliveryMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled proof of delivery mapping. */
data class ScheduledProofOfDeliveryMappingValidator(
    var rules: MutableList<ScheduledProofOfDeliveryMappingRule>,
) {
    fun validate(item: ScheduledProofOfDeliveryMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledProofOfDeliveryMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledProofOfDeliveryMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledProofOfDeliveryMappingValidator(): ScheduledProofOfDeliveryMappingValidator {
    return ScheduledProofOfDeliveryMappingValidator(
        mutableListOf<ScheduledProofOfDeliveryMappingRule>(
            ScheduledProofOfDeliveryMappingAllowedCodeRule(mutableListOf<String>()),
            ScheduledProofOfDeliveryMappingRequiredNotesRule(
                "scheduled proof of delivery mapping needs an entry",
            ),
            ScheduledProofOfDeliveryMappingRequiredNameRule(
                "scheduled proof of delivery mapping needs this field",
            ),
            ScheduledProofOfDeliveryMappingCountryLengthRule(16),
            ScheduledProofOfDeliveryMappingNameLengthRule(3600),
            ScheduledProofOfDeliveryMappingCodePrefixRule("REF"),
        ),
    )
}
