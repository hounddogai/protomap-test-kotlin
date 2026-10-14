package com.acme.transport.terminal.dockdoor.exception

/** A rule a dock door exception must pass. Check returns its problems, or none. */
interface DockDoorExceptionRule {
    fun check(item: DockDoorException): MutableList<String>
}

data class DockDoorExceptionApprovedGuardRule(var limit: Int) : DockDoorExceptionRule {
    override fun check(item: DockDoorException): MutableList<String> {
        if (item.approved && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorExceptionRequiredCodeRule(var message: String) : DockDoorExceptionRule {
    override fun check(item: DockDoorException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorExceptionTitleLengthRule(var maxLength: Int) : DockDoorExceptionRule {
    override fun check(item: DockDoorException): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door exception. */
data class DockDoorExceptionValidator(var rules: MutableList<DockDoorExceptionRule>) {
    fun validate(item: DockDoorException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorException>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorExceptionValidator(): DockDoorExceptionValidator {
    return DockDoorExceptionValidator(
        mutableListOf<DockDoorExceptionRule>(
            DockDoorExceptionApprovedGuardRule(16),
            DockDoorExceptionRequiredCodeRule("dock door exception needs a value"),
            DockDoorExceptionTitleLengthRule(64),
        ),
    )
}
