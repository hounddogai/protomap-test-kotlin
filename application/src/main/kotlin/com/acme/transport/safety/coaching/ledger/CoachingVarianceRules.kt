package com.acme.transport.safety.coaching.ledger

/** A rule a coaching variance must pass. Check returns its problems, or none. */
interface CoachingVarianceRule {
    fun check(item: CoachingVariance): MutableList<String>
}

data class CoachingVarianceCodeLengthRule(var maxLength: Int) : CoachingVarianceRule {
    override fun check(item: CoachingVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingVarianceRequiredRegionRule(var message: String) : CoachingVarianceRule {
    override fun check(item: CoachingVariance): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingVarianceRegionLengthRule(var maxLength: Int) : CoachingVarianceRule {
    override fun check(item: CoachingVariance): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingVarianceCodePrefixRule(var prefix: String) : CoachingVarianceRule {
    override fun check(item: CoachingVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingVarianceRequiredNameRule(var message: String) : CoachingVarianceRule {
    override fun check(item: CoachingVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingVarianceSequenceRangeRule(var min: Int, var max: Int) : CoachingVarianceRule {
    override fun check(item: CoachingVariance): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching variance. */
data class CoachingVarianceValidator(var rules: MutableList<CoachingVarianceRule>) {
    fun validate(item: CoachingVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingVarianceValidator(): CoachingVarianceValidator {
    return CoachingVarianceValidator(
        mutableListOf<CoachingVarianceRule>(
            CoachingVarianceCodeLengthRule(255),
            CoachingVarianceRequiredRegionRule("coaching variance needs this field"),
            CoachingVarianceRegionLengthRule(16),
            CoachingVarianceCodePrefixRule("REF"),
            CoachingVarianceRequiredNameRule("coaching variance needs an entry"),
            CoachingVarianceSequenceRangeRule(120, 3600),
        ),
    )
}
