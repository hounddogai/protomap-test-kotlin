package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.text.normalize

/** A rule a parcel segment must pass. Check returns its problems, or none. */
interface ParcelSegmentRule {
    fun check(item: ParcelSegment): MutableList<String>
}

data class ParcelSegmentAllowedCodeRule(var allowed: MutableList<String>) : ParcelSegmentRule {
    override fun check(item: ParcelSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ParcelSegmentRequiredReviewerRule(var message: String) : ParcelSegmentRule {
    override fun check(item: ParcelSegment): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelSegmentRequiredCodeRule(var message: String) : ParcelSegmentRule {
    override fun check(item: ParcelSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel segment. */
data class ParcelSegmentValidator(var rules: MutableList<ParcelSegmentRule>) {
    fun validate(item: ParcelSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelSegmentValidator(): ParcelSegmentValidator {
    return ParcelSegmentValidator(
        mutableListOf<ParcelSegmentRule>(
            ParcelSegmentAllowedCodeRule(mutableListOf<String>("manual", "adaptive", "strict")),
            ParcelSegmentRequiredReviewerRule("parcel segment needs a value"),
            ParcelSegmentRequiredCodeRule("parcel segment needs this field"),
        ),
    )
}
