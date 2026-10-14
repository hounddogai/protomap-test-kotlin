package com.acme.transport.terminal.dwell.preference

/** A rule a dwell segment must pass. Check returns its problems, or none. */
interface DwellSegmentRule {
    fun check(item: DwellSegment): MutableList<String>
}

data class DwellSegmentRequiredOwnerIdRule(var message: String) : DwellSegmentRule {
    override fun check(item: DwellSegment): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellSegmentOwnerIdLengthRule(var maxLength: Int) : DwellSegmentRule {
    override fun check(item: DwellSegment): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellSegmentRatioRangeRule(var min: Double, var max: Double) : DwellSegmentRule {
    override fun check(item: DwellSegment): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class DwellSegmentCostRangeRule(var min: Double, var max: Double) : DwellSegmentRule {
    override fun check(item: DwellSegment): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellSegmentCreatedAtRangeRule(var min: Int, var max: Int) : DwellSegmentRule {
    override fun check(item: DwellSegment): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell segment. */
data class DwellSegmentValidator(var rules: MutableList<DwellSegmentRule>) {
    fun validate(item: DwellSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellSegmentValidator(): DwellSegmentValidator {
    return DwellSegmentValidator(
        mutableListOf<DwellSegmentRule>(
            DwellSegmentRequiredOwnerIdRule("dwell segment needs a value"),
            DwellSegmentOwnerIdLengthRule(64),
            DwellSegmentRatioRangeRule(5.1, 3324.2),
            DwellSegmentCostRangeRule(4.9, 2139.1),
            DwellSegmentCreatedAtRangeRule(32, 86400),
        ),
    )
}
