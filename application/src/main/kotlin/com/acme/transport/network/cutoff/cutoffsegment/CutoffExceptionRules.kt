package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.normalize

/** A rule a cutoff exception must pass. Check returns its problems, or none. */
interface CutoffExceptionRule {
    fun check(item: CutoffException): MutableList<String>
}

data class CutoffExceptionMinimumRangeRule(var min: Double, var max: Double) : CutoffExceptionRule {
    override fun check(item: CutoffException): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class CutoffExceptionLoadKgRangeRule(var min: Double, var max: Double) : CutoffExceptionRule {
    override fun check(item: CutoffException): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class CutoffExceptionRequiredTitleRule(var message: String) : CutoffExceptionRule {
    override fun check(item: CutoffException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffExceptionRatioRangeRule(var min: Double, var max: Double) : CutoffExceptionRule {
    override fun check(item: CutoffException): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffExceptionAllowedCodeRule(var allowed: MutableList<String>) : CutoffExceptionRule {
    override fun check(item: CutoffException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff exception. */
data class CutoffExceptionValidator(var rules: MutableList<CutoffExceptionRule>) {
    fun validate(item: CutoffException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffException>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffExceptionValidator(): CutoffExceptionValidator {
    return CutoffExceptionValidator(
        mutableListOf<CutoffExceptionRule>(
            CutoffExceptionMinimumRangeRule(3.0, 1991.5),
            CutoffExceptionLoadKgRangeRule(4.3, 1761.3),
            CutoffExceptionRequiredTitleRule("cutoff exception needs this field"),
            CutoffExceptionRatioRangeRule(7.2, 3960.0),
            CutoffExceptionAllowedCodeRule(mutableListOf<String>("global", "fallback")),
        ),
    )
}
