package com.acme.transport.network.cutoff.cutofftarget

/** A rule a adaptive cutoff variance must pass. Check returns its problems, or none. */
interface AdaptiveCutoffVarianceRule {
    fun check(item: AdaptiveCutoffVariance): MutableList<String>
}

data class AdaptiveCutoffVarianceFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCutoffVarianceRule {
    override fun check(item: AdaptiveCutoffVariance): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCutoffVarianceRequiredTitleRule(var message: String) : AdaptiveCutoffVarianceRule {
    override fun check(item: AdaptiveCutoffVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCutoffVarianceEtaMinutesRangeRule(var min: Int, var max: Int) : AdaptiveCutoffVarianceRule {
    override fun check(item: AdaptiveCutoffVariance): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCutoffVarianceCodePrefixRule(var prefix: String) : AdaptiveCutoffVarianceRule {
    override fun check(item: AdaptiveCutoffVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive cutoff variance. */
data class AdaptiveCutoffVarianceValidator(var rules: MutableList<AdaptiveCutoffVarianceRule>) {
    fun validate(item: AdaptiveCutoffVariance): MutableList<String> {
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

    fun requireValid(item: AdaptiveCutoffVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCutoffVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCutoffVarianceValidator(): AdaptiveCutoffVarianceValidator {
    return AdaptiveCutoffVarianceValidator(
        mutableListOf<AdaptiveCutoffVarianceRule>(
            AdaptiveCutoffVarianceFuelLitersRangeRule(3.0, 4380.6),
            AdaptiveCutoffVarianceRequiredTitleRule("adaptive cutoff variance needs this field"),
            AdaptiveCutoffVarianceEtaMinutesRangeRule(80, 255),
            AdaptiveCutoffVarianceCodePrefixRule("ACM"),
        ),
    )
}
