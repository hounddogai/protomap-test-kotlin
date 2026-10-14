package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.text.normalize

/** A rule a capped idling target must pass. Check returns its problems, or none. */
interface CappedIdlingTargetRule {
    fun check(item: CappedIdlingTarget): MutableList<String>
}

data class CappedIdlingTargetExpiresAtRangeRule(var min: Int, var max: Int) : CappedIdlingTargetRule {
    override fun check(item: CappedIdlingTarget): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedIdlingTargetRegionLengthRule(var maxLength: Int) : CappedIdlingTargetRule {
    override fun check(item: CappedIdlingTarget): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedIdlingTargetAllowedTimezoneRule(var allowed: MutableList<String>) : CappedIdlingTargetRule {
    override fun check(item: CappedIdlingTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class CappedIdlingTargetCreatedAtRangeRule(var min: Int, var max: Int) : CappedIdlingTargetRule {
    override fun check(item: CappedIdlingTarget): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped idling target. */
data class CappedIdlingTargetValidator(var rules: MutableList<CappedIdlingTargetRule>) {
    fun validate(item: CappedIdlingTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedIdlingTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedIdlingTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedIdlingTargetValidator(): CappedIdlingTargetValidator {
    return CappedIdlingTargetValidator(
        mutableListOf<CappedIdlingTargetRule>(
            CappedIdlingTargetExpiresAtRangeRule(16, 86400),
            CappedIdlingTargetRegionLengthRule(16),
            CappedIdlingTargetAllowedTimezoneRule(mutableListOf<String>("adaptive")),
            CappedIdlingTargetCreatedAtRangeRule(16, 120),
        ),
    )
}
