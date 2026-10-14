package com.acme.transport.emissions.evcharge.proposal

/** A rule a tiered ev charge proposal must pass. Check returns its problems, or none. */
interface TieredEvChargeProposalRule {
    fun check(item: TieredEvChargeProposal): MutableList<String>
}

data class TieredEvChargeProposalCodePrefixRule(var prefix: String) : TieredEvChargeProposalRule {
    override fun check(item: TieredEvChargeProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeProposalRequiredUnitRule(var message: String) : TieredEvChargeProposalRule {
    override fun check(item: TieredEvChargeProposal): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeProposalQuantityRangeRule(var min: Int, var max: Int) : TieredEvChargeProposalRule {
    override fun check(item: TieredEvChargeProposal): MutableList<String> {
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

data class TieredEvChargeProposalRequiredLabelRule(var message: String) : TieredEvChargeProposalRule {
    override fun check(item: TieredEvChargeProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeProposalRequiredOwnerIdRule(var message: String) : TieredEvChargeProposalRule {
    override fun check(item: TieredEvChargeProposal): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeProposalRequiredCodeRule(var message: String) : TieredEvChargeProposalRule {
    override fun check(item: TieredEvChargeProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered ev charge proposal. */
data class TieredEvChargeProposalValidator(var rules: MutableList<TieredEvChargeProposalRule>) {
    fun validate(item: TieredEvChargeProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredEvChargeProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEvChargeProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEvChargeProposalValidator(): TieredEvChargeProposalValidator {
    return TieredEvChargeProposalValidator(
        mutableListOf<TieredEvChargeProposalRule>(
            TieredEvChargeProposalCodePrefixRule("Q"),
            TieredEvChargeProposalRequiredUnitRule("tiered ev charge proposal needs this field"),
            TieredEvChargeProposalQuantityRangeRule(16, 1000),
            TieredEvChargeProposalRequiredLabelRule("tiered ev charge proposal needs a value"),
            TieredEvChargeProposalRequiredOwnerIdRule("tiered ev charge proposal needs this field"),
            TieredEvChargeProposalRequiredCodeRule("tiered ev charge proposal needs an entry"),
        ),
    )
}
