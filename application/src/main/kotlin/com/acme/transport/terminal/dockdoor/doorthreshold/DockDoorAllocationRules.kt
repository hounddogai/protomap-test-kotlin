package com.acme.transport.terminal.dockdoor.doorthreshold

/** A rule a dock door allocation must pass. Check returns its problems, or none. */
interface DockDoorAllocationRule {
    fun check(item: DockDoorAllocation): MutableList<String>
}

data class DockDoorAllocationTitleLengthRule(var maxLength: Int) : DockDoorAllocationRule {
    override fun check(item: DockDoorAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorAllocationCodeLengthRule(var maxLength: Int) : DockDoorAllocationRule {
    override fun check(item: DockDoorAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorAllocationDriverIdLengthRule(var maxLength: Int) : DockDoorAllocationRule {
    override fun check(item: DockDoorAllocation): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door allocation. */
data class DockDoorAllocationValidator(var rules: MutableList<DockDoorAllocationRule>) {
    fun validate(item: DockDoorAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorAllocationValidator(): DockDoorAllocationValidator {
    return DockDoorAllocationValidator(
        mutableListOf<DockDoorAllocationRule>(
            DockDoorAllocationTitleLengthRule(80),
            DockDoorAllocationCodeLengthRule(120),
            DockDoorAllocationDriverIdLengthRule(3600),
        ),
    )
}
