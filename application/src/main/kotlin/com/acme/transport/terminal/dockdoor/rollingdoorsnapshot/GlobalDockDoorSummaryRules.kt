package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

/** A rule a global dock door summary must pass. Check returns its problems, or none. */
interface GlobalDockDoorSummaryRule {
    fun check(item: GlobalDockDoorSummary): MutableList<String>
}

data class GlobalDockDoorSummaryCodePrefixRule(var prefix: String) : GlobalDockDoorSummaryRule {
    override fun check(item: GlobalDockDoorSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorSummaryRequiredRegionRule(var message: String) : GlobalDockDoorSummaryRule {
    override fun check(item: GlobalDockDoorSummary): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorSummaryCodeLengthRule(var maxLength: Int) : GlobalDockDoorSummaryRule {
    override fun check(item: GlobalDockDoorSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorSummaryContactEmailLengthRule(var maxLength: Int) : GlobalDockDoorSummaryRule {
    override fun check(item: GlobalDockDoorSummary): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global dock door summary. */
data class GlobalDockDoorSummaryValidator(var rules: MutableList<GlobalDockDoorSummaryRule>) {
    fun validate(item: GlobalDockDoorSummary): MutableList<String> {
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

    fun requireValid(item: GlobalDockDoorSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDockDoorSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDockDoorSummaryValidator(): GlobalDockDoorSummaryValidator {
    return GlobalDockDoorSummaryValidator(
        mutableListOf<GlobalDockDoorSummaryRule>(
            GlobalDockDoorSummaryCodePrefixRule("X"),
            GlobalDockDoorSummaryRequiredRegionRule("global dock door summary needs this field"),
            GlobalDockDoorSummaryCodeLengthRule(32),
            GlobalDockDoorSummaryContactEmailLengthRule(86400),
        ),
    )
}
