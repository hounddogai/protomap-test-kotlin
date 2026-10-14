package com.acme.transport.network.zone.reservation

/** A rule a batched zone segment must pass. Check returns its problems, or none. */
interface BatchedZoneSegmentRule {
    fun check(item: BatchedZoneSegment): MutableList<String>
}

data class BatchedZoneSegmentNameLengthRule(var maxLength: Int) : BatchedZoneSegmentRule {
    override fun check(item: BatchedZoneSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneSegmentExpiresAtRangeRule(var min: Int, var max: Int) : BatchedZoneSegmentRule {
    override fun check(item: BatchedZoneSegment): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneSegmentRequiredNotesRule(var message: String) : BatchedZoneSegmentRule {
    override fun check(item: BatchedZoneSegment): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneSegmentCodePrefixRule(var prefix: String) : BatchedZoneSegmentRule {
    override fun check(item: BatchedZoneSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneSegmentRequiredCodeRule(var message: String) : BatchedZoneSegmentRule {
    override fun check(item: BatchedZoneSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneSegmentCodeLengthRule(var maxLength: Int) : BatchedZoneSegmentRule {
    override fun check(item: BatchedZoneSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched zone segment. */
data class BatchedZoneSegmentValidator(var rules: MutableList<BatchedZoneSegmentRule>) {
    fun validate(item: BatchedZoneSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedZoneSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedZoneSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedZoneSegmentValidator(): BatchedZoneSegmentValidator {
    return BatchedZoneSegmentValidator(
        mutableListOf<BatchedZoneSegmentRule>(
            BatchedZoneSegmentNameLengthRule(16),
            BatchedZoneSegmentExpiresAtRangeRule(1000, 3600),
            BatchedZoneSegmentRequiredNotesRule("batched zone segment needs an entry"),
            BatchedZoneSegmentCodePrefixRule("REF"),
            BatchedZoneSegmentRequiredCodeRule("batched zone segment needs this field"),
            BatchedZoneSegmentCodeLengthRule(64),
        ),
    )
}
