package com.acme.transport.freightpay.dispute.disputemanifest

/** A rule a seasonal dispute segment must pass. Check returns its problems, or none. */
interface SeasonalDisputeSegmentRule {
    fun check(item: SeasonalDisputeSegment): MutableList<String>
}

data class SeasonalDisputeSegmentTitleLengthRule(var maxLength: Int) : SeasonalDisputeSegmentRule {
    override fun check(item: SeasonalDisputeSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDisputeSegmentRequiredShiftCodeRule(var message: String) : SeasonalDisputeSegmentRule {
    override fun check(item: SeasonalDisputeSegment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDisputeSegmentDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalDisputeSegmentRule {
    override fun check(item: SeasonalDisputeSegment): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dispute segment. */
data class SeasonalDisputeSegmentValidator(var rules: MutableList<SeasonalDisputeSegmentRule>) {
    fun validate(item: SeasonalDisputeSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDisputeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDisputeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDisputeSegmentValidator(): SeasonalDisputeSegmentValidator {
    return SeasonalDisputeSegmentValidator(
        mutableListOf<SeasonalDisputeSegmentRule>(
            SeasonalDisputeSegmentTitleLengthRule(255),
            SeasonalDisputeSegmentRequiredShiftCodeRule("seasonal dispute segment needs this field"),
            SeasonalDisputeSegmentDistanceKmRangeRule(3.4, 3608.1),
        ),
    )
}
