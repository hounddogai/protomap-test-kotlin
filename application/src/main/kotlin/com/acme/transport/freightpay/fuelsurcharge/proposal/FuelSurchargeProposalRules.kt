package com.acme.transport.freightpay.fuelsurcharge.proposal

/** A rule a fuel surcharge proposal must pass. Check returns its problems, or none. */
interface FuelSurchargeProposalRule {
    fun check(item: FuelSurchargeProposal): MutableList<String>
}

data class FuelSurchargeProposalCodePrefixRule(var prefix: String) : FuelSurchargeProposalRule {
    override fun check(item: FuelSurchargeProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeProposalLabelLengthRule(var maxLength: Int) : FuelSurchargeProposalRule {
    override fun check(item: FuelSurchargeProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeProposalExpiresAtRangeRule(var min: Int, var max: Int) : FuelSurchargeProposalRule {
    override fun check(item: FuelSurchargeProposal): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeProposalRequiredCodeRule(var message: String) : FuelSurchargeProposalRule {
    override fun check(item: FuelSurchargeProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeProposalPalletsRangeRule(var min: Int, var max: Int) : FuelSurchargeProposalRule {
    override fun check(item: FuelSurchargeProposal): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeProposalCodeLengthRule(var maxLength: Int) : FuelSurchargeProposalRule {
    override fun check(item: FuelSurchargeProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge proposal. */
data class FuelSurchargeProposalValidator(var rules: MutableList<FuelSurchargeProposalRule>) {
    fun validate(item: FuelSurchargeProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeProposalValidator(): FuelSurchargeProposalValidator {
    return FuelSurchargeProposalValidator(
        mutableListOf<FuelSurchargeProposalRule>(
            FuelSurchargeProposalCodePrefixRule("X"),
            FuelSurchargeProposalLabelLengthRule(86400),
            FuelSurchargeProposalExpiresAtRangeRule(1000, 86400),
            FuelSurchargeProposalRequiredCodeRule("fuel surcharge proposal needs a value"),
            FuelSurchargeProposalPalletsRangeRule(1000, 3600),
            FuelSurchargeProposalCodeLengthRule(16),
        ),
    )
}
