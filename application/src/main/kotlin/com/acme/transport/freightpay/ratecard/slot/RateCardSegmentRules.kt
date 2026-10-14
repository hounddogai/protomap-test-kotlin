package com.acme.transport.freightpay.ratecard.slot

/** A rule a rate card segment must pass. Check returns its problems, or none. */
interface RateCardSegmentRule {
    fun check(item: RateCardSegment): MutableList<String>
}

data class RateCardSegmentOwnerIdLengthRule(var maxLength: Int) : RateCardSegmentRule {
    override fun check(item: RateCardSegment): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSegmentBudgetRangeRule(var min: Double, var max: Double) : RateCardSegmentRule {
    override fun check(item: RateCardSegment): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSegmentVisibleGuardRule(var limit: Double) : RateCardSegmentRule {
    override fun check(item: RateCardSegment): MutableList<String> {
        if (item.visible && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSegmentRequiredDockRule(var message: String) : RateCardSegmentRule {
    override fun check(item: RateCardSegment): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardSegmentDockLengthRule(var maxLength: Int) : RateCardSegmentRule {
    override fun check(item: RateCardSegment): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card segment. */
data class RateCardSegmentValidator(var rules: MutableList<RateCardSegmentRule>) {
    fun validate(item: RateCardSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardSegmentValidator(): RateCardSegmentValidator {
    return RateCardSegmentValidator(
        mutableListOf<RateCardSegmentRule>(
            RateCardSegmentOwnerIdLengthRule(255),
            RateCardSegmentBudgetRangeRule(8.5, 384.7),
            RateCardSegmentVisibleGuardRule(7.8),
            RateCardSegmentRequiredDockRule("rate card segment needs a value"),
            RateCardSegmentDockLengthRule(255),
        ),
    )
}
