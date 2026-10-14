package com.acme.transport.terminal.dockdoor.cycle

/** A rule a dock door cycle must pass. Check returns its problems, or none. */
interface DockDoorCycleRule {
    fun check(item: DockDoorCycle): MutableList<String>
}

data class DockDoorCycleSummaryLengthRule(var maxLength: Int) : DockDoorCycleRule {
    override fun check(item: DockDoorCycle): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorCycleActiveGuardRule(var limit: Double) : DockDoorCycleRule {
    override fun check(item: DockDoorCycle): MutableList<String> {
        if (item.active && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorCycleRequiredCodeRule(var message: String) : DockDoorCycleRule {
    override fun check(item: DockDoorCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorCycleCodeLengthRule(var maxLength: Int) : DockDoorCycleRule {
    override fun check(item: DockDoorCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorCycleTitleLengthRule(var maxLength: Int) : DockDoorCycleRule {
    override fun check(item: DockDoorCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door cycle. */
data class DockDoorCycleValidator(var rules: MutableList<DockDoorCycleRule>) {
    fun validate(item: DockDoorCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorCycleValidator(): DockDoorCycleValidator {
    return DockDoorCycleValidator(
        mutableListOf<DockDoorCycleRule>(
            DockDoorCycleSummaryLengthRule(120),
            DockDoorCycleActiveGuardRule(5.1),
            DockDoorCycleRequiredCodeRule("dock door cycle needs this field"),
            DockDoorCycleCodeLengthRule(255),
            DockDoorCycleTitleLengthRule(3600),
        ),
    )
}
