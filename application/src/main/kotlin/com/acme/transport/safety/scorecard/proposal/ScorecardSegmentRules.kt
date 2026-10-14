package com.acme.transport.safety.scorecard.proposal

/** A rule a scorecard segment must pass. Check returns its problems, or none. */
interface ScorecardSegmentRule {
    fun check(item: ScorecardSegment): MutableList<String>
}

data class ScorecardSegmentRequiredCarrierRule(var message: String) : ScorecardSegmentRule {
    override fun check(item: ScorecardSegment): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSegmentEtaMinutesRangeRule(var min: Int, var max: Int) : ScorecardSegmentRule {
    override fun check(item: ScorecardSegment): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSegmentPriorityRangeRule(var min: Int, var max: Int) : ScorecardSegmentRule {
    override fun check(item: ScorecardSegment): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class ScorecardSegmentRequiredLaneRule(var message: String) : ScorecardSegmentRule {
    override fun check(item: ScorecardSegment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSegmentRequiredLabelRule(var message: String) : ScorecardSegmentRule {
    override fun check(item: ScorecardSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard segment. */
data class ScorecardSegmentValidator(var rules: MutableList<ScorecardSegmentRule>) {
    fun validate(item: ScorecardSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardSegmentValidator(): ScorecardSegmentValidator {
    return ScorecardSegmentValidator(
        mutableListOf<ScorecardSegmentRule>(
            ScorecardSegmentRequiredCarrierRule("scorecard segment needs this field"),
            ScorecardSegmentEtaMinutesRangeRule(32, 3600),
            ScorecardSegmentPriorityRangeRule(64, 80),
            ScorecardSegmentRequiredLaneRule("scorecard segment needs an entry"),
            ScorecardSegmentRequiredLabelRule("scorecard segment needs a value"),
        ),
    )
}
