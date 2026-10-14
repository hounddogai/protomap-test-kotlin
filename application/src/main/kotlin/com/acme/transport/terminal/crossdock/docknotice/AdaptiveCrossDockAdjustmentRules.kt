package com.acme.transport.terminal.crossdock.docknotice

/** A rule a adaptive cross dock adjustment must pass. Check returns its problems, or none. */
interface AdaptiveCrossDockAdjustmentRule {
    fun check(item: AdaptiveCrossDockAdjustment): MutableList<String>
}

data class AdaptiveCrossDockAdjustmentLabelLengthRule(var maxLength: Int) : AdaptiveCrossDockAdjustmentRule {
    override fun check(item: AdaptiveCrossDockAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockAdjustmentRatioRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCrossDockAdjustmentRule {
    override fun check(item: AdaptiveCrossDockAdjustment): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockAdjustmentLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCrossDockAdjustmentRule {
    override fun check(item: AdaptiveCrossDockAdjustment): MutableList<String> {
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

data class AdaptiveCrossDockAdjustmentRequiredPhoneRule(var message: String) : AdaptiveCrossDockAdjustmentRule {
    override fun check(item: AdaptiveCrossDockAdjustment): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockAdjustmentRequiredCountryRule(
    var message: String,
) : AdaptiveCrossDockAdjustmentRule {
    override fun check(item: AdaptiveCrossDockAdjustment): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockAdjustmentCodeLengthRule(var maxLength: Int) : AdaptiveCrossDockAdjustmentRule {
    override fun check(item: AdaptiveCrossDockAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive cross dock adjustment. */
data class AdaptiveCrossDockAdjustmentValidator(var rules: MutableList<AdaptiveCrossDockAdjustmentRule>) {
    fun validate(item: AdaptiveCrossDockAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveCrossDockAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCrossDockAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCrossDockAdjustmentValidator(): AdaptiveCrossDockAdjustmentValidator {
    return AdaptiveCrossDockAdjustmentValidator(
        mutableListOf<AdaptiveCrossDockAdjustmentRule>(
            AdaptiveCrossDockAdjustmentLabelLengthRule(255),
            AdaptiveCrossDockAdjustmentRatioRangeRule(2.6, 4270.5),
            AdaptiveCrossDockAdjustmentLoadKgRangeRule(8.9, 4352.9),
            AdaptiveCrossDockAdjustmentRequiredPhoneRule("adaptive cross dock adjustment needs a value"),
            AdaptiveCrossDockAdjustmentRequiredCountryRule("adaptive cross dock adjustment needs this field"),
            AdaptiveCrossDockAdjustmentCodeLengthRule(255),
        ),
    )
}
