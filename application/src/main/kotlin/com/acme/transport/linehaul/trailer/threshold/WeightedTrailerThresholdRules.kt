package com.acme.transport.linehaul.trailer.threshold

/** A rule a weighted trailer threshold must pass. Check returns its problems, or none. */
interface WeightedTrailerThresholdRule {
    fun check(item: WeightedTrailerThreshold): MutableList<String>
}

data class WeightedTrailerThresholdRequiredNameRule(var message: String) : WeightedTrailerThresholdRule {
    override fun check(item: WeightedTrailerThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerThresholdOwnerIdLengthRule(var maxLength: Int) : WeightedTrailerThresholdRule {
    override fun check(item: WeightedTrailerThreshold): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerThresholdRatioRangeRule(
    var min: Double,
    var max: Double,
) : WeightedTrailerThresholdRule {
    override fun check(item: WeightedTrailerThreshold): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerThresholdLabelLengthRule(var maxLength: Int) : WeightedTrailerThresholdRule {
    override fun check(item: WeightedTrailerThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerThresholdCodeLengthRule(var maxLength: Int) : WeightedTrailerThresholdRule {
    override fun check(item: WeightedTrailerThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerThresholdMaximumRangeRule(
    var min: Double,
    var max: Double,
) : WeightedTrailerThresholdRule {
    override fun check(item: WeightedTrailerThreshold): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted trailer threshold. */
data class WeightedTrailerThresholdValidator(var rules: MutableList<WeightedTrailerThresholdRule>) {
    fun validate(item: WeightedTrailerThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTrailerThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTrailerThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTrailerThresholdValidator(): WeightedTrailerThresholdValidator {
    return WeightedTrailerThresholdValidator(
        mutableListOf<WeightedTrailerThresholdRule>(
            WeightedTrailerThresholdRequiredNameRule("weighted trailer threshold needs this field"),
            WeightedTrailerThresholdOwnerIdLengthRule(80),
            WeightedTrailerThresholdRatioRangeRule(1.1, 4683.1),
            WeightedTrailerThresholdLabelLengthRule(86400),
            WeightedTrailerThresholdCodeLengthRule(120),
            WeightedTrailerThresholdMaximumRangeRule(8.4, 906.1),
        ),
    )
}
