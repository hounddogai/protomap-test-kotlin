package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.text.normalize

/** A rule a default dock door segment must pass. Check returns its problems, or none. */
interface DefaultDockDoorSegmentRule {
    fun check(item: DefaultDockDoorSegment): MutableList<String>
}

data class DefaultDockDoorSegmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : DefaultDockDoorSegmentRule {
    override fun check(item: DefaultDockDoorSegment): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDockDoorSegmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : DefaultDockDoorSegmentRule {
    override fun check(item: DefaultDockDoorSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDockDoorSegmentRequiredTitleRule(var message: String) : DefaultDockDoorSegmentRule {
    override fun check(item: DefaultDockDoorSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDockDoorSegmentDockLengthRule(var maxLength: Int) : DefaultDockDoorSegmentRule {
    override fun check(item: DefaultDockDoorSegment): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default dock door segment. */
data class DefaultDockDoorSegmentValidator(var rules: MutableList<DefaultDockDoorSegmentRule>) {
    fun validate(item: DefaultDockDoorSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDockDoorSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDockDoorSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDockDoorSegmentValidator(): DefaultDockDoorSegmentValidator {
    return DefaultDockDoorSegmentValidator(
        mutableListOf<DefaultDockDoorSegmentRule>(
            DefaultDockDoorSegmentThresholdRangeRule(1.0, 4239.0),
            DefaultDockDoorSegmentAllowedTitleRule(mutableListOf<String>("tiered", "default")),
            DefaultDockDoorSegmentRequiredTitleRule("default dock door segment needs an entry"),
            DefaultDockDoorSegmentDockLengthRule(16),
        ),
    )
}
