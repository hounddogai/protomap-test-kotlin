package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.text.normalize

/** A rule a dock door baseline must pass. Check returns its problems, or none. */
interface DockDoorBaselineRule {
    fun check(item: DockDoorBaseline): MutableList<String>
}

data class DockDoorBaselineAllowedLabelRule(var allowed: MutableList<String>) : DockDoorBaselineRule {
    override fun check(item: DockDoorBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorBaselineLabelLengthRule(var maxLength: Int) : DockDoorBaselineRule {
    override fun check(item: DockDoorBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorBaselineSegmentLengthRule(var maxLength: Int) : DockDoorBaselineRule {
    override fun check(item: DockDoorBaseline): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorBaselineReviewerLengthRule(var maxLength: Int) : DockDoorBaselineRule {
    override fun check(item: DockDoorBaseline): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorBaselineCodeLengthRule(var maxLength: Int) : DockDoorBaselineRule {
    override fun check(item: DockDoorBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door baseline. */
data class DockDoorBaselineValidator(var rules: MutableList<DockDoorBaselineRule>) {
    fun validate(item: DockDoorBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorBaselineValidator(): DockDoorBaselineValidator {
    return DockDoorBaselineValidator(
        mutableListOf<DockDoorBaselineRule>(
            DockDoorBaselineAllowedLabelRule(mutableListOf<String>("scheduled")),
            DockDoorBaselineLabelLengthRule(80),
            DockDoorBaselineSegmentLengthRule(32),
            DockDoorBaselineReviewerLengthRule(64),
            DockDoorBaselineCodeLengthRule(32),
        ),
    )
}
