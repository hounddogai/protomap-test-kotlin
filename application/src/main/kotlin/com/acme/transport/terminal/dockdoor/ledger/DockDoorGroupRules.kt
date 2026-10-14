package com.acme.transport.terminal.dockdoor.ledger

/** A rule a dock door group must pass. Check returns its problems, or none. */
interface DockDoorGroupRule {
    fun check(item: DockDoorGroup): MutableList<String>
}

data class DockDoorGroupReviewerLengthRule(var maxLength: Int) : DockDoorGroupRule {
    override fun check(item: DockDoorGroup): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorGroupRevisionRangeRule(var min: Int, var max: Int) : DockDoorGroupRule {
    override fun check(item: DockDoorGroup): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorGroupLabelLengthRule(var maxLength: Int) : DockDoorGroupRule {
    override fun check(item: DockDoorGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door group. */
data class DockDoorGroupValidator(var rules: MutableList<DockDoorGroupRule>) {
    fun validate(item: DockDoorGroup): MutableList<String> {
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

    fun requireValid(item: DockDoorGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorGroupValidator(): DockDoorGroupValidator {
    return DockDoorGroupValidator(
        mutableListOf<DockDoorGroupRule>(
            DockDoorGroupReviewerLengthRule(1000),
            DockDoorGroupRevisionRangeRule(120, 120),
            DockDoorGroupLabelLengthRule(255),
        ),
    )
}
