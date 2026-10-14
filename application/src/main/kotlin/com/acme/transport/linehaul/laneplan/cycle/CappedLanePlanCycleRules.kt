package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.normalize

/** A rule a capped lane plan cycle must pass. Check returns its problems, or none. */
interface CappedLanePlanCycleRule {
    fun check(item: CappedLanePlanCycle): MutableList<String>
}

data class CappedLanePlanCycleAllowedTitleRule(var allowed: MutableList<String>) : CappedLanePlanCycleRule {
    override fun check(item: CappedLanePlanCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanCycleReviewerLengthRule(var maxLength: Int) : CappedLanePlanCycleRule {
    override fun check(item: CappedLanePlanCycle): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanCycleRequiredCodeRule(var message: String) : CappedLanePlanCycleRule {
    override fun check(item: CappedLanePlanCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped lane plan cycle. */
data class CappedLanePlanCycleValidator(var rules: MutableList<CappedLanePlanCycleRule>) {
    fun validate(item: CappedLanePlanCycle): MutableList<String> {
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

    fun requireValid(item: CappedLanePlanCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLanePlanCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLanePlanCycleValidator(): CappedLanePlanCycleValidator {
    return CappedLanePlanCycleValidator(
        mutableListOf<CappedLanePlanCycleRule>(
            CappedLanePlanCycleAllowedTitleRule(mutableListOf<String>("seasonal")),
            CappedLanePlanCycleReviewerLengthRule(3600),
            CappedLanePlanCycleRequiredCodeRule("capped lane plan cycle needs this field"),
        ),
    )
}
