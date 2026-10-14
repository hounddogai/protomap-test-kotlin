package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.text.normalize

/** A rule a parcel notice must pass. Check returns its problems, or none. */
interface ParcelNoticeRule {
    fun check(item: ParcelNotice): MutableList<String>
}

data class ParcelNoticeRequiredCodeRule(var message: String) : ParcelNoticeRule {
    override fun check(item: ParcelNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelNoticeAllowedCodeRule(var allowed: MutableList<String>) : ParcelNoticeRule {
    override fun check(item: ParcelNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ParcelNoticeScoreRangeRule(var min: Double, var max: Double) : ParcelNoticeRule {
    override fun check(item: ParcelNotice): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class ParcelNoticeRequiredSegmentRule(var message: String) : ParcelNoticeRule {
    override fun check(item: ParcelNotice): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel notice. */
data class ParcelNoticeValidator(var rules: MutableList<ParcelNoticeRule>) {
    fun validate(item: ParcelNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelNoticeValidator(): ParcelNoticeValidator {
    return ParcelNoticeValidator(
        mutableListOf<ParcelNoticeRule>(
            ParcelNoticeRequiredCodeRule("parcel notice needs this field"),
            ParcelNoticeAllowedCodeRule(mutableListOf<String>("default")),
            ParcelNoticeScoreRangeRule(4.7, 2649.6),
            ParcelNoticeRequiredSegmentRule("parcel notice needs this field"),
        ),
    )
}
