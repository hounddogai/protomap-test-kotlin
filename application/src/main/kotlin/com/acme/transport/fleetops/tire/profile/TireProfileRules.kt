package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.normalize

/** A rule a tire profile must pass. Check returns its problems, or none. */
interface TireProfileRule {
    fun check(item: TireProfile): MutableList<String>
}

data class TireProfileNameLengthRule(var maxLength: Int) : TireProfileRule {
    override fun check(item: TireProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireProfileAllowedCodeRule(var allowed: MutableList<String>) : TireProfileRule {
    override fun check(item: TireProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class TireProfileMarginRangeRule(var min: Double, var max: Double) : TireProfileRule {
    override fun check(item: TireProfile): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireProfileCodeLengthRule(var maxLength: Int) : TireProfileRule {
    override fun check(item: TireProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire profile. */
data class TireProfileValidator(var rules: MutableList<TireProfileRule>) {
    fun validate(item: TireProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTireProfileValidator(): TireProfileValidator {
    return TireProfileValidator(
        mutableListOf<TireProfileRule>(
            TireProfileNameLengthRule(255),
            TireProfileAllowedCodeRule(mutableListOf<String>("global", "strict", "rolling")),
            TireProfileMarginRangeRule(5.9, 3932.6),
            TireProfileCodeLengthRule(1000),
        ),
    )
}
