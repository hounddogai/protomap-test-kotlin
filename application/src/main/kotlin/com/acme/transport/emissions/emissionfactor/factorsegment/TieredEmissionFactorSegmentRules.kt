package com.acme.transport.emissions.emissionfactor.factorsegment

/** A rule a tiered emission factor segment must pass. Check returns its problems, or none. */
interface TieredEmissionFactorSegmentRule {
    fun check(item: TieredEmissionFactorSegment): MutableList<String>
}

data class TieredEmissionFactorSegmentCodePrefixRule(var prefix: String) : TieredEmissionFactorSegmentRule {
    override fun check(item: TieredEmissionFactorSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorSegmentDepotLengthRule(var maxLength: Int) : TieredEmissionFactorSegmentRule {
    override fun check(item: TieredEmissionFactorSegment): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorSegmentNameLengthRule(var maxLength: Int) : TieredEmissionFactorSegmentRule {
    override fun check(item: TieredEmissionFactorSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorSegmentRequiredDepotRule(var message: String) : TieredEmissionFactorSegmentRule {
    override fun check(item: TieredEmissionFactorSegment): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered emission factor segment. */
data class TieredEmissionFactorSegmentValidator(var rules: MutableList<TieredEmissionFactorSegmentRule>) {
    fun validate(item: TieredEmissionFactorSegment): MutableList<String> {
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

    fun requireValid(item: TieredEmissionFactorSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEmissionFactorSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEmissionFactorSegmentValidator(): TieredEmissionFactorSegmentValidator {
    return TieredEmissionFactorSegmentValidator(
        mutableListOf<TieredEmissionFactorSegmentRule>(
            TieredEmissionFactorSegmentCodePrefixRule("OPS"),
            TieredEmissionFactorSegmentDepotLengthRule(64),
            TieredEmissionFactorSegmentNameLengthRule(32),
            TieredEmissionFactorSegmentRequiredDepotRule("tiered emission factor segment needs an entry"),
        ),
    )
}
