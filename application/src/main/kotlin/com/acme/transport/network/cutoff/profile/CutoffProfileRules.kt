package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.normalize

/** A rule a cutoff profile must pass. Check returns its problems, or none. */
interface CutoffProfileRule {
    fun check(item: CutoffProfile): MutableList<String>
}

data class CutoffProfileRequiredUnitRule(var message: String) : CutoffProfileRule {
    override fun check(item: CutoffProfile): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffProfileSourceLengthRule(var maxLength: Int) : CutoffProfileRule {
    override fun check(item: CutoffProfile): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffProfileWindowDaysRangeRule(var min: Int, var max: Int) : CutoffProfileRule {
    override fun check(item: CutoffProfile): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffProfileAllowedNameRule(var allowed: MutableList<String>) : CutoffProfileRule {
    override fun check(item: CutoffProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class CutoffProfileRequiredNameRule(var message: String) : CutoffProfileRule {
    override fun check(item: CutoffProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff profile. */
data class CutoffProfileValidator(var rules: MutableList<CutoffProfileRule>) {
    fun validate(item: CutoffProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffProfileValidator(): CutoffProfileValidator {
    return CutoffProfileValidator(
        mutableListOf<CutoffProfileRule>(
            CutoffProfileRequiredUnitRule("cutoff profile needs a value"),
            CutoffProfileSourceLengthRule(32),
            CutoffProfileWindowDaysRangeRule(16, 120),
            CutoffProfileAllowedNameRule(mutableListOf<String>("pending", "capped", "lenient")),
            CutoffProfileRequiredNameRule("cutoff profile needs this field"),
        ),
    )
}
