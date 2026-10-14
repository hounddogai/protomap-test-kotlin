package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.text.normalize

/** A rule a service area proposal must pass. Check returns its problems, or none. */
interface ServiceAreaProposalRule {
    fun check(item: ServiceAreaProposal): MutableList<String>
}

data class ServiceAreaProposalLabelLengthRule(var maxLength: Int) : ServiceAreaProposalRule {
    override fun check(item: ServiceAreaProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaProposalMinimumRangeRule(var min: Double, var max: Double) : ServiceAreaProposalRule {
    override fun check(item: ServiceAreaProposal): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaProposalRequiredShiftCodeRule(var message: String) : ServiceAreaProposalRule {
    override fun check(item: ServiceAreaProposal): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaProposalCodePrefixRule(var prefix: String) : ServiceAreaProposalRule {
    override fun check(item: ServiceAreaProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaProposalAllowedShiftCodeRule(var allowed: MutableList<String>) : ServiceAreaProposalRule {
    override fun check(item: ServiceAreaProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaProposalBatchSizeRangeRule(var min: Int, var max: Int) : ServiceAreaProposalRule {
    override fun check(item: ServiceAreaProposal): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a service area proposal. */
data class ServiceAreaProposalValidator(var rules: MutableList<ServiceAreaProposalRule>) {
    fun validate(item: ServiceAreaProposal): MutableList<String> {
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

    fun requireValid(item: ServiceAreaProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaProposalValidator(): ServiceAreaProposalValidator {
    return ServiceAreaProposalValidator(
        mutableListOf<ServiceAreaProposalRule>(
            ServiceAreaProposalLabelLengthRule(86400),
            ServiceAreaProposalMinimumRangeRule(2.5, 3341.5),
            ServiceAreaProposalRequiredShiftCodeRule("service area proposal needs this field"),
            ServiceAreaProposalCodePrefixRule("OPS"),
            ServiceAreaProposalAllowedShiftCodeRule(mutableListOf<String>("capped")),
            ServiceAreaProposalBatchSizeRangeRule(86400, 86400),
        ),
    )
}
