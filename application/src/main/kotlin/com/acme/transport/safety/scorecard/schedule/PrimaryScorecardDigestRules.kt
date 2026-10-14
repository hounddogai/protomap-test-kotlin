package com.acme.transport.safety.scorecard.schedule

/** A rule a primary scorecard digest must pass. Check returns its problems, or none. */
interface PrimaryScorecardDigestRule {
    fun check(item: PrimaryScorecardDigest): MutableList<String>
}

data class PrimaryScorecardDigestLaneLengthRule(var maxLength: Int) : PrimaryScorecardDigestRule {
    override fun check(item: PrimaryScorecardDigest): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardDigestCodeLengthRule(var maxLength: Int) : PrimaryScorecardDigestRule {
    override fun check(item: PrimaryScorecardDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardDigestEtaMinutesRangeRule(var min: Int, var max: Int) : PrimaryScorecardDigestRule {
    override fun check(item: PrimaryScorecardDigest): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class PrimaryScorecardDigestCodePrefixRule(var prefix: String) : PrimaryScorecardDigestRule {
    override fun check(item: PrimaryScorecardDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardDigestRequiredLaneRule(var message: String) : PrimaryScorecardDigestRule {
    override fun check(item: PrimaryScorecardDigest): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary scorecard digest. */
data class PrimaryScorecardDigestValidator(var rules: MutableList<PrimaryScorecardDigestRule>) {
    fun validate(item: PrimaryScorecardDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryScorecardDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryScorecardDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryScorecardDigestValidator(): PrimaryScorecardDigestValidator {
    return PrimaryScorecardDigestValidator(
        mutableListOf<PrimaryScorecardDigestRule>(
            PrimaryScorecardDigestLaneLengthRule(3600),
            PrimaryScorecardDigestCodeLengthRule(3600),
            PrimaryScorecardDigestEtaMinutesRangeRule(120, 3600),
            PrimaryScorecardDigestCodePrefixRule("Q"),
            PrimaryScorecardDigestRequiredLaneRule("primary scorecard digest needs this field"),
        ),
    )
}
