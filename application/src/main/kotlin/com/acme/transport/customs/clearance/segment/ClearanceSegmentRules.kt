package com.acme.transport.customs.clearance.segment

/** A rule a clearance segment must pass. Check returns its problems, or none. */
interface ClearanceSegmentRule {
    fun check(item: ClearanceSegment): MutableList<String>
}

data class ClearanceSegmentCodePrefixRule(var prefix: String) : ClearanceSegmentRule {
    override fun check(item: ClearanceSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSegmentRequiredTitleRule(var message: String) : ClearanceSegmentRule {
    override fun check(item: ClearanceSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSegmentLoadKgRangeRule(var min: Double, var max: Double) : ClearanceSegmentRule {
    override fun check(item: ClearanceSegment): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSegmentRequiredCodeRule(var message: String) : ClearanceSegmentRule {
    override fun check(item: ClearanceSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSegmentTitleLengthRule(var maxLength: Int) : ClearanceSegmentRule {
    override fun check(item: ClearanceSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance segment. */
data class ClearanceSegmentValidator(var rules: MutableList<ClearanceSegmentRule>) {
    fun validate(item: ClearanceSegment): MutableList<String> {
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

    fun requireValid(item: ClearanceSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceSegmentValidator(): ClearanceSegmentValidator {
    return ClearanceSegmentValidator(
        mutableListOf<ClearanceSegmentRule>(
            ClearanceSegmentCodePrefixRule("ACM"),
            ClearanceSegmentRequiredTitleRule("clearance segment needs this field"),
            ClearanceSegmentLoadKgRangeRule(4.6, 4812.9),
            ClearanceSegmentRequiredCodeRule("clearance segment needs an entry"),
            ClearanceSegmentTitleLengthRule(16),
        ),
    )
}
