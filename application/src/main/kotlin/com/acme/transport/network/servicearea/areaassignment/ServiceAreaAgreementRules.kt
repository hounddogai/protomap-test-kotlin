package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.normalize

/** A rule a service area agreement must pass. Check returns its problems, or none. */
interface ServiceAreaAgreementRule {
    fun check(item: ServiceAreaAgreement): MutableList<String>
}

data class ServiceAreaAgreementRequiredOwnerIdRule(var message: String) : ServiceAreaAgreementRule {
    override fun check(item: ServiceAreaAgreement): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAgreementAllowedTitleRule(var allowed: MutableList<String>) : ServiceAreaAgreementRule {
    override fun check(item: ServiceAreaAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAgreementOwnerIdLengthRule(var maxLength: Int) : ServiceAreaAgreementRule {
    override fun check(item: ServiceAreaAgreement): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAgreementCodeLengthRule(var maxLength: Int) : ServiceAreaAgreementRule {
    override fun check(item: ServiceAreaAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAgreementCodePrefixRule(var prefix: String) : ServiceAreaAgreementRule {
    override fun check(item: ServiceAreaAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area agreement. */
data class ServiceAreaAgreementValidator(var rules: MutableList<ServiceAreaAgreementRule>) {
    fun validate(item: ServiceAreaAgreement): MutableList<String> {
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

    fun requireValid(item: ServiceAreaAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaAgreementValidator(): ServiceAreaAgreementValidator {
    return ServiceAreaAgreementValidator(
        mutableListOf<ServiceAreaAgreementRule>(
            ServiceAreaAgreementRequiredOwnerIdRule("service area agreement needs this field"),
            ServiceAreaAgreementAllowedTitleRule(mutableListOf<String>("default")),
            ServiceAreaAgreementOwnerIdLengthRule(255),
            ServiceAreaAgreementCodeLengthRule(32),
            ServiceAreaAgreementCodePrefixRule("Q"),
        ),
    )
}
