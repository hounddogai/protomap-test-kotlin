package com.acme.transport.customs.declaration.budget

/** A rule a declaration proposal must pass. Check returns its problems, or none. */
interface DeclarationProposalRule {
    fun check(item: DeclarationProposal): MutableList<String>
}

data class DeclarationProposalPalletsRangeRule(var min: Int, var max: Int) : DeclarationProposalRule {
    override fun check(item: DeclarationProposal): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class DeclarationProposalRequiredNameRule(var message: String) : DeclarationProposalRule {
    override fun check(item: DeclarationProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationProposalCodeLengthRule(var maxLength: Int) : DeclarationProposalRule {
    override fun check(item: DeclarationProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationProposalRequiredVehicleIdRule(var message: String) : DeclarationProposalRule {
    override fun check(item: DeclarationProposal): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationProposalNameLengthRule(var maxLength: Int) : DeclarationProposalRule {
    override fun check(item: DeclarationProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration proposal. */
data class DeclarationProposalValidator(var rules: MutableList<DeclarationProposalRule>) {
    fun validate(item: DeclarationProposal): MutableList<String> {
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

    fun requireValid(item: DeclarationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationProposalValidator(): DeclarationProposalValidator {
    return DeclarationProposalValidator(
        mutableListOf<DeclarationProposalRule>(
            DeclarationProposalPalletsRangeRule(64, 86400),
            DeclarationProposalRequiredNameRule("declaration proposal needs this field"),
            DeclarationProposalCodeLengthRule(3600),
            DeclarationProposalRequiredVehicleIdRule("declaration proposal needs an entry"),
            DeclarationProposalNameLengthRule(120),
        ),
    )
}
