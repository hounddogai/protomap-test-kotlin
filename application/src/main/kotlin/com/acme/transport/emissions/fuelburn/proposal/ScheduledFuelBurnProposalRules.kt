package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.text.normalize

/** A rule a scheduled fuel burn proposal must pass. Check returns its problems, or none. */
interface ScheduledFuelBurnProposalRule {
    fun check(item: ScheduledFuelBurnProposal): MutableList<String>
}

data class ScheduledFuelBurnProposalContactEmailLengthRule(var maxLength: Int) : ScheduledFuelBurnProposalRule {
    override fun check(item: ScheduledFuelBurnProposal): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnProposalRequiredCodeRule(var message: String) : ScheduledFuelBurnProposalRule {
    override fun check(item: ScheduledFuelBurnProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnProposalCeilingRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledFuelBurnProposalRule {
    override fun check(item: ScheduledFuelBurnProposal): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnProposalAllowedNameRule(
    var allowed: MutableList<String>,
) : ScheduledFuelBurnProposalRule {
    override fun check(item: ScheduledFuelBurnProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnProposalFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledFuelBurnProposalRule {
    override fun check(item: ScheduledFuelBurnProposal): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled fuel burn proposal. */
data class ScheduledFuelBurnProposalValidator(var rules: MutableList<ScheduledFuelBurnProposalRule>) {
    fun validate(item: ScheduledFuelBurnProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledFuelBurnProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFuelBurnProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFuelBurnProposalValidator(): ScheduledFuelBurnProposalValidator {
    return ScheduledFuelBurnProposalValidator(
        mutableListOf<ScheduledFuelBurnProposalRule>(
            ScheduledFuelBurnProposalContactEmailLengthRule(255),
            ScheduledFuelBurnProposalRequiredCodeRule("scheduled fuel burn proposal needs a value"),
            ScheduledFuelBurnProposalCeilingRangeRule(2.8, 3418.6),
            ScheduledFuelBurnProposalAllowedNameRule(mutableListOf<String>()),
            ScheduledFuelBurnProposalFuelLitersRangeRule(4.9, 1004.5),
        ),
    )
}
