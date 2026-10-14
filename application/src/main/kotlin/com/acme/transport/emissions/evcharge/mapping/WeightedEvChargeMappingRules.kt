package com.acme.transport.emissions.evcharge.mapping

/** A rule a weighted ev charge mapping must pass. Check returns its problems, or none. */
interface WeightedEvChargeMappingRule {
    fun check(item: WeightedEvChargeMapping): MutableList<String>
}

data class WeightedEvChargeMappingEndsAtRangeRule(var min: Int, var max: Int) : WeightedEvChargeMappingRule {
    override fun check(item: WeightedEvChargeMapping): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class WeightedEvChargeMappingPinnedGuardRule(var limit: Double) : WeightedEvChargeMappingRule {
    override fun check(item: WeightedEvChargeMapping): MutableList<String> {
        if (item.pinned && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeMappingCodeLengthRule(var maxLength: Int) : WeightedEvChargeMappingRule {
    override fun check(item: WeightedEvChargeMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeMappingDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : WeightedEvChargeMappingRule {
    override fun check(item: WeightedEvChargeMapping): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class WeightedEvChargeMappingRequiredNameRule(var message: String) : WeightedEvChargeMappingRule {
    override fun check(item: WeightedEvChargeMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted ev charge mapping. */
data class WeightedEvChargeMappingValidator(var rules: MutableList<WeightedEvChargeMappingRule>) {
    fun validate(item: WeightedEvChargeMapping): MutableList<String> {
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

    fun requireValid(item: WeightedEvChargeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEvChargeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEvChargeMappingValidator(): WeightedEvChargeMappingValidator {
    return WeightedEvChargeMappingValidator(
        mutableListOf<WeightedEvChargeMappingRule>(
            WeightedEvChargeMappingEndsAtRangeRule(255, 3600),
            WeightedEvChargeMappingPinnedGuardRule(4.4),
            WeightedEvChargeMappingCodeLengthRule(86400),
            WeightedEvChargeMappingDistanceKmRangeRule(8.5, 2934.7),
            WeightedEvChargeMappingRequiredNameRule("weighted ev charge mapping needs this field"),
        ),
    )
}
