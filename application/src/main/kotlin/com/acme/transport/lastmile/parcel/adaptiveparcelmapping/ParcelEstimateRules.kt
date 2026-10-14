package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.text.normalize

/** A rule a parcel estimate must pass. Check returns its problems, or none. */
interface ParcelEstimateRule {
    fun check(item: ParcelEstimate): MutableList<String>
}

data class ParcelEstimateAllowedRegionRule(var allowed: MutableList<String>) : ParcelEstimateRule {
    override fun check(item: ParcelEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ParcelEstimateRequiredCodeRule(var message: String) : ParcelEstimateRule {
    override fun check(item: ParcelEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelEstimateCodePrefixRule(var prefix: String) : ParcelEstimateRule {
    override fun check(item: ParcelEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ParcelEstimateBudgetRangeRule(var min: Double, var max: Double) : ParcelEstimateRule {
    override fun check(item: ParcelEstimate): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel estimate. */
data class ParcelEstimateValidator(var rules: MutableList<ParcelEstimateRule>) {
    fun validate(item: ParcelEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelEstimateValidator(): ParcelEstimateValidator {
    return ParcelEstimateValidator(
        mutableListOf<ParcelEstimateRule>(
            ParcelEstimateAllowedRegionRule(mutableListOf<String>()),
            ParcelEstimateRequiredCodeRule("parcel estimate needs this field"),
            ParcelEstimateCodePrefixRule("OPS"),
            ParcelEstimateBudgetRangeRule(9.8, 3219.8),
        ),
    )
}
