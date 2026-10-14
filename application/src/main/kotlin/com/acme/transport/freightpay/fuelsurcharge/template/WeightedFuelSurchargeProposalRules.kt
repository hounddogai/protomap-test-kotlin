package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.text.normalize

/** A rule a weighted fuel surcharge proposal must pass. Check returns its problems, or none. */
interface WeightedFuelSurchargeProposalRule {
    fun check(item: WeightedFuelSurchargeProposal): MutableList<String>
}

data class WeightedFuelSurchargeProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedFuelSurchargeProposalRule {
    override fun check(item: WeightedFuelSurchargeProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeProposalContactEmailLengthRule(
    var maxLength: Int,
) : WeightedFuelSurchargeProposalRule {
    override fun check(item: WeightedFuelSurchargeProposal): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeProposalRetriesRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFuelSurchargeProposalRule {
    override fun check(item: WeightedFuelSurchargeProposal): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeProposalRequiredCodeRule(
    var message: String,
) : WeightedFuelSurchargeProposalRule {
    override fun check(item: WeightedFuelSurchargeProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeProposalRequiredNameRule(
    var message: String,
) : WeightedFuelSurchargeProposalRule {
    override fun check(item: WeightedFuelSurchargeProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeProposalQuantityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFuelSurchargeProposalRule {
    override fun check(item: WeightedFuelSurchargeProposal): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted fuel surcharge proposal. */
data class WeightedFuelSurchargeProposalValidator(var rules: MutableList<WeightedFuelSurchargeProposalRule>) {
    fun validate(item: WeightedFuelSurchargeProposal): MutableList<String> {
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

    fun requireValid(item: WeightedFuelSurchargeProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFuelSurchargeProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFuelSurchargeProposalValidator(): WeightedFuelSurchargeProposalValidator {
    return WeightedFuelSurchargeProposalValidator(
        mutableListOf<WeightedFuelSurchargeProposalRule>(
            WeightedFuelSurchargeProposalAllowedCodeRule(mutableListOf<String>()),
            WeightedFuelSurchargeProposalContactEmailLengthRule(120),
            WeightedFuelSurchargeProposalRetriesRangeRule(80, 80),
            WeightedFuelSurchargeProposalRequiredCodeRule("weighted fuel surcharge proposal needs this field"),
            WeightedFuelSurchargeProposalRequiredNameRule("weighted fuel surcharge proposal needs an entry"),
            WeightedFuelSurchargeProposalQuantityRangeRule(255, 1000),
        ),
    )
}
