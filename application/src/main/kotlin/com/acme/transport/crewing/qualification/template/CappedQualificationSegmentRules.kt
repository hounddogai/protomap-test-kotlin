package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.text.normalize

/** A rule a capped qualification segment must pass. Check returns its problems, or none. */
interface CappedQualificationSegmentRule {
    fun check(item: CappedQualificationSegment): MutableList<String>
}

data class CappedQualificationSegmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : CappedQualificationSegmentRule {
    override fun check(item: CappedQualificationSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationSegmentCodePrefixRule(var prefix: String) : CappedQualificationSegmentRule {
    override fun check(item: CappedQualificationSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationSegmentRequiredNotesRule(var message: String) : CappedQualificationSegmentRule {
    override fun check(item: CappedQualificationSegment): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationSegmentVisibleGuardRule(var limit: Double) : CappedQualificationSegmentRule {
    override fun check(item: CappedQualificationSegment): MutableList<String> {
        if (item.visible && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationSegmentNotesLengthRule(var maxLength: Int) : CappedQualificationSegmentRule {
    override fun check(item: CappedQualificationSegment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped qualification segment. */
data class CappedQualificationSegmentValidator(var rules: MutableList<CappedQualificationSegmentRule>) {
    fun validate(item: CappedQualificationSegment): MutableList<String> {
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

    fun requireValid(item: CappedQualificationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedQualificationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedQualificationSegmentValidator(): CappedQualificationSegmentValidator {
    return CappedQualificationSegmentValidator(
        mutableListOf<CappedQualificationSegmentRule>(
            CappedQualificationSegmentAllowedTitleRule(
                mutableListOf<String>("regional", "scheduled", "batched"),
            ),
            CappedQualificationSegmentCodePrefixRule("REF"),
            CappedQualificationSegmentRequiredNotesRule("capped qualification segment needs a value"),
            CappedQualificationSegmentVisibleGuardRule(0.8),
            CappedQualificationSegmentNotesLengthRule(80),
        ),
    )
}
