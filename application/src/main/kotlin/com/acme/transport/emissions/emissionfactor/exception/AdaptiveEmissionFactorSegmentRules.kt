package com.acme.transport.emissions.emissionfactor.exception

/** A rule a adaptive emission factor segment must pass. Check returns its problems, or none. */
interface AdaptiveEmissionFactorSegmentRule {
    fun check(item: AdaptiveEmissionFactorSegment): MutableList<String>
}

data class AdaptiveEmissionFactorSegmentRequiredCarrierRule(
    var message: String,
) : AdaptiveEmissionFactorSegmentRule {
    override fun check(item: AdaptiveEmissionFactorSegment): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorSegmentMaximumRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveEmissionFactorSegmentRule {
    override fun check(item: AdaptiveEmissionFactorSegment): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorSegmentRevisionRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveEmissionFactorSegmentRule {
    override fun check(item: AdaptiveEmissionFactorSegment): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorSegmentCarrierLengthRule(
    var maxLength: Int,
) : AdaptiveEmissionFactorSegmentRule {
    override fun check(item: AdaptiveEmissionFactorSegment): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorSegmentRequiredUnitRule(
    var message: String,
) : AdaptiveEmissionFactorSegmentRule {
    override fun check(item: AdaptiveEmissionFactorSegment): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive emission factor segment. */
data class AdaptiveEmissionFactorSegmentValidator(var rules: MutableList<AdaptiveEmissionFactorSegmentRule>) {
    fun validate(item: AdaptiveEmissionFactorSegment): MutableList<String> {
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

    fun requireValid(item: AdaptiveEmissionFactorSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEmissionFactorSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEmissionFactorSegmentValidator(): AdaptiveEmissionFactorSegmentValidator {
    return AdaptiveEmissionFactorSegmentValidator(
        mutableListOf<AdaptiveEmissionFactorSegmentRule>(
            AdaptiveEmissionFactorSegmentRequiredCarrierRule("adaptive emission factor segment needs an entry"),
            AdaptiveEmissionFactorSegmentMaximumRangeRule(9.9, 4992.2),
            AdaptiveEmissionFactorSegmentRevisionRangeRule(80, 1000),
            AdaptiveEmissionFactorSegmentCarrierLengthRule(80),
            AdaptiveEmissionFactorSegmentRequiredUnitRule("adaptive emission factor segment needs an entry"),
        ),
    )
}
