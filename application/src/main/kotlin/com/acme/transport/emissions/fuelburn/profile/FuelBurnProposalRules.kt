package com.acme.transport.emissions.fuelburn.profile

/** A rule a fuel burn proposal must pass. Check returns its problems, or none. */
interface FuelBurnProposalRule {
    fun check(item: FuelBurnProposal): MutableList<String>
}

data class FuelBurnProposalRequiredVehicleIdRule(var message: String) : FuelBurnProposalRule {
    override fun check(item: FuelBurnProposal): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnProposalRequiredCodeRule(var message: String) : FuelBurnProposalRule {
    override fun check(item: FuelBurnProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnProposalRequiredShiftCodeRule(var message: String) : FuelBurnProposalRule {
    override fun check(item: FuelBurnProposal): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnProposalRequiredNameRule(var message: String) : FuelBurnProposalRule {
    override fun check(item: FuelBurnProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnProposalCodePrefixRule(var prefix: String) : FuelBurnProposalRule {
    override fun check(item: FuelBurnProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn proposal. */
data class FuelBurnProposalValidator(var rules: MutableList<FuelBurnProposalRule>) {
    fun validate(item: FuelBurnProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnProposalValidator(): FuelBurnProposalValidator {
    return FuelBurnProposalValidator(
        mutableListOf<FuelBurnProposalRule>(
            FuelBurnProposalRequiredVehicleIdRule("fuel burn proposal needs a value"),
            FuelBurnProposalRequiredCodeRule("fuel burn proposal needs a value"),
            FuelBurnProposalRequiredShiftCodeRule("fuel burn proposal needs a value"),
            FuelBurnProposalRequiredNameRule("fuel burn proposal needs a value"),
            FuelBurnProposalCodePrefixRule("ACM"),
        ),
    )
}
