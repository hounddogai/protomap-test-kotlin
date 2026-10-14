package com.acme.transport.safety.scorecard.scorecardgroup

/** A rule a adaptive scorecard segment must pass. Check returns its problems, or none. */
interface AdaptiveScorecardSegmentRule {
    fun check(item: AdaptiveScorecardSegment): MutableList<String>
}

data class AdaptiveScorecardSegmentRequiredChecksumRule(var message: String) : AdaptiveScorecardSegmentRule {
    override fun check(item: AdaptiveScorecardSegment): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardSegmentTitleLengthRule(var maxLength: Int) : AdaptiveScorecardSegmentRule {
    override fun check(item: AdaptiveScorecardSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardSegmentCodeLengthRule(var maxLength: Int) : AdaptiveScorecardSegmentRule {
    override fun check(item: AdaptiveScorecardSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardSegmentRequiredCodeRule(var message: String) : AdaptiveScorecardSegmentRule {
    override fun check(item: AdaptiveScorecardSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardSegmentChecksumLengthRule(var maxLength: Int) : AdaptiveScorecardSegmentRule {
    override fun check(item: AdaptiveScorecardSegment): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive scorecard segment. */
data class AdaptiveScorecardSegmentValidator(var rules: MutableList<AdaptiveScorecardSegmentRule>) {
    fun validate(item: AdaptiveScorecardSegment): MutableList<String> {
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

    fun requireValid(item: AdaptiveScorecardSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveScorecardSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveScorecardSegmentValidator(): AdaptiveScorecardSegmentValidator {
    return AdaptiveScorecardSegmentValidator(
        mutableListOf<AdaptiveScorecardSegmentRule>(
            AdaptiveScorecardSegmentRequiredChecksumRule("adaptive scorecard segment needs a value"),
            AdaptiveScorecardSegmentTitleLengthRule(86400),
            AdaptiveScorecardSegmentCodeLengthRule(16),
            AdaptiveScorecardSegmentRequiredCodeRule("adaptive scorecard segment needs this field"),
            AdaptiveScorecardSegmentChecksumLengthRule(80),
        ),
    )
}
