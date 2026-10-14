package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a regional defect cycle must pass. Check returns its problems, or none. */
interface RegionalDefectCycleRule {
    fun check(item: RegionalDefectCycle): MutableList<String>
}

data class RegionalDefectCycleRequiredCodeRule(var message: String) : RegionalDefectCycleRule {
    override fun check(item: RegionalDefectCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectCycleScoreRangeRule(var min: Double, var max: Double) : RegionalDefectCycleRule {
    override fun check(item: RegionalDefectCycle): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectCycleAllowedReviewerRule(var allowed: MutableList<String>) : RegionalDefectCycleRule {
    override fun check(item: RegionalDefectCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectCycleRequiredReviewerRule(var message: String) : RegionalDefectCycleRule {
    override fun check(item: RegionalDefectCycle): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional defect cycle. */
data class RegionalDefectCycleValidator(var rules: MutableList<RegionalDefectCycleRule>) {
    fun validate(item: RegionalDefectCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDefectCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDefectCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDefectCycleValidator(): RegionalDefectCycleValidator {
    return RegionalDefectCycleValidator(
        mutableListOf<RegionalDefectCycleRule>(
            RegionalDefectCycleRequiredCodeRule("regional defect cycle needs an entry"),
            RegionalDefectCycleScoreRangeRule(1.5, 166.6),
            RegionalDefectCycleAllowedReviewerRule(mutableListOf<String>("default")),
            RegionalDefectCycleRequiredReviewerRule("regional defect cycle needs an entry"),
        ),
    )
}
