package com.acme.transport.terminal.sortation.manifest

/** A rule a pending sortation segment must pass. Check returns its problems, or none. */
interface PendingSortationSegmentRule {
    fun check(item: PendingSortationSegment): MutableList<String>
}

data class PendingSortationSegmentPriorityRangeRule(var min: Int, var max: Int) : PendingSortationSegmentRule {
    override fun check(item: PendingSortationSegment): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationSegmentRequiredNameRule(var message: String) : PendingSortationSegmentRule {
    override fun check(item: PendingSortationSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationSegmentFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PendingSortationSegmentRule {
    override fun check(item: PendingSortationSegment): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationSegmentRequiredDescriptionRule(var message: String) : PendingSortationSegmentRule {
    override fun check(item: PendingSortationSegment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationSegmentExpiresAtRangeRule(var min: Int, var max: Int) : PendingSortationSegmentRule {
    override fun check(item: PendingSortationSegment): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationSegmentCodePrefixRule(var prefix: String) : PendingSortationSegmentRule {
    override fun check(item: PendingSortationSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending sortation segment. */
data class PendingSortationSegmentValidator(var rules: MutableList<PendingSortationSegmentRule>) {
    fun validate(item: PendingSortationSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingSortationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingSortationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingSortationSegmentValidator(): PendingSortationSegmentValidator {
    return PendingSortationSegmentValidator(
        mutableListOf<PendingSortationSegmentRule>(
            PendingSortationSegmentPriorityRangeRule(86400, 86400),
            PendingSortationSegmentRequiredNameRule("pending sortation segment needs this field"),
            PendingSortationSegmentFloorValueRangeRule(3.9, 4156.5),
            PendingSortationSegmentRequiredDescriptionRule("pending sortation segment needs an entry"),
            PendingSortationSegmentExpiresAtRangeRule(32, 64),
            PendingSortationSegmentCodePrefixRule("ACM"),
        ),
    )
}
