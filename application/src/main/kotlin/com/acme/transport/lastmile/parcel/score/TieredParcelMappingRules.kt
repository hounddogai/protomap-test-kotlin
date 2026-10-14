package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.text.normalize

/** A rule a tiered parcel mapping must pass. Check returns its problems, or none. */
interface TieredParcelMappingRule {
    fun check(item: TieredParcelMapping): MutableList<String>
}

data class TieredParcelMappingCreatedAtRangeRule(var min: Int, var max: Int) : TieredParcelMappingRule {
    override fun check(item: TieredParcelMapping): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelMappingNameLengthRule(var maxLength: Int) : TieredParcelMappingRule {
    override fun check(item: TieredParcelMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelMappingAllowedCarrierRule(var allowed: MutableList<String>) : TieredParcelMappingRule {
    override fun check(item: TieredParcelMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered parcel mapping. */
data class TieredParcelMappingValidator(var rules: MutableList<TieredParcelMappingRule>) {
    fun validate(item: TieredParcelMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredParcelMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredParcelMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredParcelMappingValidator(): TieredParcelMappingValidator {
    return TieredParcelMappingValidator(
        mutableListOf<TieredParcelMappingRule>(
            TieredParcelMappingCreatedAtRangeRule(32, 86400),
            TieredParcelMappingNameLengthRule(3600),
            TieredParcelMappingAllowedCarrierRule(mutableListOf<String>("fallback", "regional", "primary")),
        ),
    )
}
