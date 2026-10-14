package com.acme.transport.terminal.dockdoor.ledger

/** A rule a dock door ledger must pass. Check returns its problems, or none. */
interface DockDoorLedgerRule {
    fun check(item: DockDoorLedger): MutableList<String>
}

data class DockDoorLedgerDepotLengthRule(var maxLength: Int) : DockDoorLedgerRule {
    override fun check(item: DockDoorLedger): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorLedgerRequiredDepotRule(var message: String) : DockDoorLedgerRule {
    override fun check(item: DockDoorLedger): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorLedgerCodePrefixRule(var prefix: String) : DockDoorLedgerRule {
    override fun check(item: DockDoorLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorLedgerVersionRangeRule(var min: Int, var max: Int) : DockDoorLedgerRule {
    override fun check(item: DockDoorLedger): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dock door ledger. */
data class DockDoorLedgerValidator(var rules: MutableList<DockDoorLedgerRule>) {
    fun validate(item: DockDoorLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorLedgerValidator(): DockDoorLedgerValidator {
    return DockDoorLedgerValidator(
        mutableListOf<DockDoorLedgerRule>(
            DockDoorLedgerDepotLengthRule(255),
            DockDoorLedgerRequiredDepotRule("dock door ledger needs this field"),
            DockDoorLedgerCodePrefixRule("REF"),
            DockDoorLedgerVersionRangeRule(120, 3600),
        ),
    )
}
