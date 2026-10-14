package com.acme.transport.terminal.dockdoor.tiereddooragreement

/** A rule a regional dock door agreement must pass. Check returns its problems, or none. */
interface RegionalDockDoorAgreementRule {
    fun check(item: RegionalDockDoorAgreement): MutableList<String>
}

data class RegionalDockDoorAgreementRequiredCodeRule(var message: String) : RegionalDockDoorAgreementRule {
    override fun check(item: RegionalDockDoorAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorAgreementCapacityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalDockDoorAgreementRule {
    override fun check(item: RegionalDockDoorAgreement): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class RegionalDockDoorAgreementPriorityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalDockDoorAgreementRule {
    override fun check(item: RegionalDockDoorAgreement): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional dock door agreement. */
data class RegionalDockDoorAgreementValidator(var rules: MutableList<RegionalDockDoorAgreementRule>) {
    fun validate(item: RegionalDockDoorAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDockDoorAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDockDoorAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDockDoorAgreementValidator(): RegionalDockDoorAgreementValidator {
    return RegionalDockDoorAgreementValidator(
        mutableListOf<RegionalDockDoorAgreementRule>(
            RegionalDockDoorAgreementRequiredCodeRule("regional dock door agreement needs a value"),
            RegionalDockDoorAgreementCapacityRangeRule(16, 120),
            RegionalDockDoorAgreementPriorityRangeRule(1000, 86400),
        ),
    )
}
