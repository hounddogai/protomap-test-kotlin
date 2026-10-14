package com.acme.transport.lastmile.parcel.baseline

/** A rule a parcel baseline must pass. Check returns its problems, or none. */
interface ParcelBaselineRule {
    fun check(item: ParcelBaseline): MutableList<String>
}

data class ParcelBaselineRequiredChannelRule(var message: String) : ParcelBaselineRule {
    override fun check(item: ParcelBaseline): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelBaselineScoreRangeRule(var min: Double, var max: Double) : ParcelBaselineRule {
    override fun check(item: ParcelBaseline): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class ParcelBaselineChannelLengthRule(var maxLength: Int) : ParcelBaselineRule {
    override fun check(item: ParcelBaseline): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelBaselineLabelLengthRule(var maxLength: Int) : ParcelBaselineRule {
    override fun check(item: ParcelBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelBaselineRequiredCodeRule(var message: String) : ParcelBaselineRule {
    override fun check(item: ParcelBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel baseline. */
data class ParcelBaselineValidator(var rules: MutableList<ParcelBaselineRule>) {
    fun validate(item: ParcelBaseline): MutableList<String> {
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

    fun requireValid(item: ParcelBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelBaselineValidator(): ParcelBaselineValidator {
    return ParcelBaselineValidator(
        mutableListOf<ParcelBaselineRule>(
            ParcelBaselineRequiredChannelRule("parcel baseline needs a value"),
            ParcelBaselineScoreRangeRule(0.7, 3936.2),
            ParcelBaselineChannelLengthRule(64),
            ParcelBaselineLabelLengthRule(1000),
            ParcelBaselineRequiredCodeRule("parcel baseline needs an entry"),
        ),
    )
}
