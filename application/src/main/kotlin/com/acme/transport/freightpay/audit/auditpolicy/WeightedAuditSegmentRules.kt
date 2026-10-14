package com.acme.transport.freightpay.audit.auditpolicy

/** A rule a weighted audit segment must pass. Check returns its problems, or none. */
interface WeightedAuditSegmentRule {
    fun check(item: WeightedAuditSegment): MutableList<String>
}

data class WeightedAuditSegmentCreatedAtRangeRule(var min: Int, var max: Int) : WeightedAuditSegmentRule {
    override fun check(item: WeightedAuditSegment): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class WeightedAuditSegmentRequiredTitleRule(var message: String) : WeightedAuditSegmentRule {
    override fun check(item: WeightedAuditSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditSegmentCodeLengthRule(var maxLength: Int) : WeightedAuditSegmentRule {
    override fun check(item: WeightedAuditSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditSegmentCapacityRangeRule(var min: Int, var max: Int) : WeightedAuditSegmentRule {
    override fun check(item: WeightedAuditSegment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditSegmentRevisionRangeRule(var min: Int, var max: Int) : WeightedAuditSegmentRule {
    override fun check(item: WeightedAuditSegment): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted audit segment. */
data class WeightedAuditSegmentValidator(var rules: MutableList<WeightedAuditSegmentRule>) {
    fun validate(item: WeightedAuditSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAuditSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAuditSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAuditSegmentValidator(): WeightedAuditSegmentValidator {
    return WeightedAuditSegmentValidator(
        mutableListOf<WeightedAuditSegmentRule>(
            WeightedAuditSegmentCreatedAtRangeRule(255, 1000),
            WeightedAuditSegmentRequiredTitleRule("weighted audit segment needs a value"),
            WeightedAuditSegmentCodeLengthRule(80),
            WeightedAuditSegmentCapacityRangeRule(32, 120),
            WeightedAuditSegmentRevisionRangeRule(16, 1000),
        ),
    )
}
