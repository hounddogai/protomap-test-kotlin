package com.acme.transport.terminal.dockdoor.mapping

/** A rule a dock door audit must pass. Check returns its problems, or none. */
interface DockDoorAuditRule {
    fun check(item: DockDoorAudit): MutableList<String>
}

data class DockDoorAuditNameLengthRule(var maxLength: Int) : DockDoorAuditRule {
    override fun check(item: DockDoorAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorAuditCostRangeRule(var min: Double, var max: Double) : DockDoorAuditRule {
    override fun check(item: DockDoorAudit): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class DockDoorAuditRequiredNameRule(var message: String) : DockDoorAuditRule {
    override fun check(item: DockDoorAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorAuditFuelLitersRangeRule(var min: Double, var max: Double) : DockDoorAuditRule {
    override fun check(item: DockDoorAudit): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dock door audit. */
data class DockDoorAuditValidator(var rules: MutableList<DockDoorAuditRule>) {
    fun validate(item: DockDoorAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorAuditValidator(): DockDoorAuditValidator {
    return DockDoorAuditValidator(
        mutableListOf<DockDoorAuditRule>(
            DockDoorAuditNameLengthRule(80),
            DockDoorAuditCostRangeRule(4.6, 1528.0),
            DockDoorAuditRequiredNameRule("dock door audit needs this field"),
            DockDoorAuditFuelLitersRangeRule(0.4, 1372.5),
        ),
    )
}
