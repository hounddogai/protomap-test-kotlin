package com.acme.transport.terminal.dockdoor.doorrecord

/** A rule a weighted dock door audit must pass. Check returns its problems, or none. */
interface WeightedDockDoorAuditRule {
    fun check(item: WeightedDockDoorAudit): MutableList<String>
}

data class WeightedDockDoorAuditCodeLengthRule(var maxLength: Int) : WeightedDockDoorAuditRule {
    override fun check(item: WeightedDockDoorAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDockDoorAuditRequiredLabelRule(var message: String) : WeightedDockDoorAuditRule {
    override fun check(item: WeightedDockDoorAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDockDoorAuditLabelLengthRule(var maxLength: Int) : WeightedDockDoorAuditRule {
    override fun check(item: WeightedDockDoorAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDockDoorAuditCodePrefixRule(var prefix: String) : WeightedDockDoorAuditRule {
    override fun check(item: WeightedDockDoorAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted dock door audit. */
data class WeightedDockDoorAuditValidator(var rules: MutableList<WeightedDockDoorAuditRule>) {
    fun validate(item: WeightedDockDoorAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedDockDoorAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDockDoorAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDockDoorAuditValidator(): WeightedDockDoorAuditValidator {
    return WeightedDockDoorAuditValidator(
        mutableListOf<WeightedDockDoorAuditRule>(
            WeightedDockDoorAuditCodeLengthRule(16),
            WeightedDockDoorAuditRequiredLabelRule("weighted dock door audit needs a value"),
            WeightedDockDoorAuditLabelLengthRule(32),
            WeightedDockDoorAuditCodePrefixRule("Q"),
        ),
    )
}
