package com.acme.transport.customs.inspection.metric

/** A rule a inspection segment must pass. Check returns its problems, or none. */
interface InspectionSegmentRule {
    fun check(item: InspectionSegment): MutableList<String>
}

data class InspectionSegmentAttemptsRangeRule(var min: Int, var max: Int) : InspectionSegmentRule {
    override fun check(item: InspectionSegment): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class InspectionSegmentRequiredPhoneRule(var message: String) : InspectionSegmentRule {
    override fun check(item: InspectionSegment): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionSegmentBatchSizeRangeRule(var min: Int, var max: Int) : InspectionSegmentRule {
    override fun check(item: InspectionSegment): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection segment. */
data class InspectionSegmentValidator(var rules: MutableList<InspectionSegmentRule>) {
    fun validate(item: InspectionSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionSegmentValidator(): InspectionSegmentValidator {
    return InspectionSegmentValidator(
        mutableListOf<InspectionSegmentRule>(
            InspectionSegmentAttemptsRangeRule(32, 3600),
            InspectionSegmentRequiredPhoneRule("inspection segment needs a value"),
            InspectionSegmentBatchSizeRangeRule(120, 3600),
        ),
    )
}
