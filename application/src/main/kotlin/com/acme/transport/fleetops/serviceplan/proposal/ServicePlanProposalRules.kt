package com.acme.transport.fleetops.serviceplan.proposal

/** A rule a service plan proposal must pass. Check returns its problems, or none. */
interface ServicePlanProposalRule {
    fun check(item: ServicePlanProposal): MutableList<String>
}

data class ServicePlanProposalCodePrefixRule(var prefix: String) : ServicePlanProposalRule {
    override fun check(item: ServicePlanProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanProposalLabelLengthRule(var maxLength: Int) : ServicePlanProposalRule {
    override fun check(item: ServicePlanProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanProposalRequiredLocaleRule(var message: String) : ServicePlanProposalRule {
    override fun check(item: ServicePlanProposal): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanProposalCodeLengthRule(var maxLength: Int) : ServicePlanProposalRule {
    override fun check(item: ServicePlanProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanProposalRequiredCodeRule(var message: String) : ServicePlanProposalRule {
    override fun check(item: ServicePlanProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan proposal. */
data class ServicePlanProposalValidator(var rules: MutableList<ServicePlanProposalRule>) {
    fun validate(item: ServicePlanProposal): MutableList<String> {
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

    fun requireValid(item: ServicePlanProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanProposalValidator(): ServicePlanProposalValidator {
    return ServicePlanProposalValidator(
        mutableListOf<ServicePlanProposalRule>(
            ServicePlanProposalCodePrefixRule("OPS"),
            ServicePlanProposalLabelLengthRule(1000),
            ServicePlanProposalRequiredLocaleRule("service plan proposal needs this field"),
            ServicePlanProposalCodeLengthRule(86400),
            ServicePlanProposalRequiredCodeRule("service plan proposal needs a value"),
        ),
    )
}
