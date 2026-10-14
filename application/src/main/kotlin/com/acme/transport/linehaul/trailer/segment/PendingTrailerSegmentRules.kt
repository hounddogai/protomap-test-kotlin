package com.acme.transport.linehaul.trailer.segment

/** A rule a pending trailer segment must pass. Check returns its problems, or none. */
interface PendingTrailerSegmentRule {
    fun check(item: PendingTrailerSegment): MutableList<String>
}

data class PendingTrailerSegmentCodeLengthRule(var maxLength: Int) : PendingTrailerSegmentRule {
    override fun check(item: PendingTrailerSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerSegmentRequiredLabelRule(var message: String) : PendingTrailerSegmentRule {
    override fun check(item: PendingTrailerSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerSegmentRequiredCodeRule(var message: String) : PendingTrailerSegmentRule {
    override fun check(item: PendingTrailerSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerSegmentTrailerIdLengthRule(var maxLength: Int) : PendingTrailerSegmentRule {
    override fun check(item: PendingTrailerSegment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTrailerSegmentRateRangeRule(var min: Double, var max: Double) : PendingTrailerSegmentRule {
    override fun check(item: PendingTrailerSegment): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending trailer segment. */
data class PendingTrailerSegmentValidator(var rules: MutableList<PendingTrailerSegmentRule>) {
    fun validate(item: PendingTrailerSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTrailerSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTrailerSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTrailerSegmentValidator(): PendingTrailerSegmentValidator {
    return PendingTrailerSegmentValidator(
        mutableListOf<PendingTrailerSegmentRule>(
            PendingTrailerSegmentCodeLengthRule(120),
            PendingTrailerSegmentRequiredLabelRule("pending trailer segment needs this field"),
            PendingTrailerSegmentRequiredCodeRule("pending trailer segment needs this field"),
            PendingTrailerSegmentTrailerIdLengthRule(120),
            PendingTrailerSegmentRateRangeRule(9.5, 856.1),
        ),
    )
}
