package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.kit.text.normalize

/** A rule a pending emission factor segment must pass. Check returns its problems, or none. */
interface PendingEmissionFactorSegmentRule {
    fun check(item: PendingEmissionFactorSegment): MutableList<String>
}

data class PendingEmissionFactorSegmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingEmissionFactorSegmentRule {
    override fun check(item: PendingEmissionFactorSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorSegmentCodeLengthRule(var maxLength: Int) : PendingEmissionFactorSegmentRule {
    override fun check(item: PendingEmissionFactorSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorSegmentCeilingRangeRule(
    var min: Double,
    var max: Double,
) : PendingEmissionFactorSegmentRule {
    override fun check(item: PendingEmissionFactorSegment): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class PendingEmissionFactorSegmentArchivedGuardRule(var limit: Double) : PendingEmissionFactorSegmentRule {
    override fun check(item: PendingEmissionFactorSegment): MutableList<String> {
        if (item.archived && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorSegmentTitleLengthRule(var maxLength: Int) : PendingEmissionFactorSegmentRule {
    override fun check(item: PendingEmissionFactorSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorSegmentRequiredTitleRule(
    var message: String,
) : PendingEmissionFactorSegmentRule {
    override fun check(item: PendingEmissionFactorSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending emission factor segment. */
data class PendingEmissionFactorSegmentValidator(var rules: MutableList<PendingEmissionFactorSegmentRule>) {
    fun validate(item: PendingEmissionFactorSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingEmissionFactorSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingEmissionFactorSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingEmissionFactorSegmentValidator(): PendingEmissionFactorSegmentValidator {
    return PendingEmissionFactorSegmentValidator(
        mutableListOf<PendingEmissionFactorSegmentRule>(
            PendingEmissionFactorSegmentAllowedCodeRule(mutableListOf<String>("seasonal")),
            PendingEmissionFactorSegmentCodeLengthRule(32),
            PendingEmissionFactorSegmentCeilingRangeRule(2.8, 1853.2),
            PendingEmissionFactorSegmentArchivedGuardRule(2.5),
            PendingEmissionFactorSegmentTitleLengthRule(80),
            PendingEmissionFactorSegmentRequiredTitleRule("pending emission factor segment needs a value"),
        ),
    )
}
