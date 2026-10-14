package com.acme.transport.linehaul.tender.quota

/** A rule a tender segment must pass. Check returns its problems, or none. */
interface TenderSegmentRule {
    fun check(item: TenderSegment): MutableList<String>
}

data class TenderSegmentLabelLengthRule(var maxLength: Int) : TenderSegmentRule {
    override fun check(item: TenderSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderSegmentSequenceRangeRule(var min: Int, var max: Int) : TenderSegmentRule {
    override fun check(item: TenderSegment): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class TenderSegmentCodePrefixRule(var prefix: String) : TenderSegmentRule {
    override fun check(item: TenderSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderSegmentRequiredLabelRule(var message: String) : TenderSegmentRule {
    override fun check(item: TenderSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender segment. */
data class TenderSegmentValidator(var rules: MutableList<TenderSegmentRule>) {
    fun validate(item: TenderSegment): MutableList<String> {
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

    fun requireValid(item: TenderSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderSegmentValidator(): TenderSegmentValidator {
    return TenderSegmentValidator(
        mutableListOf<TenderSegmentRule>(
            TenderSegmentLabelLengthRule(64),
            TenderSegmentSequenceRangeRule(32, 255),
            TenderSegmentCodePrefixRule("OPS"),
            TenderSegmentRequiredLabelRule("tender segment needs a value"),
        ),
    )
}
