package com.acme.transport.terminal.dwell.metric

/** A rule a dwell review must pass. Check returns its problems, or none. */
interface DwellReviewRule {
    fun check(item: DwellReview): MutableList<String>
}

data class DwellReviewEndsAtRangeRule(var min: Int, var max: Int) : DwellReviewRule {
    override fun check(item: DwellReview): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellReviewRequiredCarrierRule(var message: String) : DwellReviewRule {
    override fun check(item: DwellReview): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellReviewTitleLengthRule(var maxLength: Int) : DwellReviewRule {
    override fun check(item: DwellReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellReviewPhoneLengthRule(var maxLength: Int) : DwellReviewRule {
    override fun check(item: DwellReview): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell review. */
data class DwellReviewValidator(var rules: MutableList<DwellReviewRule>) {
    fun validate(item: DwellReview): MutableList<String> {
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

    fun requireValid(item: DwellReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellReviewValidator(): DwellReviewValidator {
    return DwellReviewValidator(
        mutableListOf<DwellReviewRule>(
            DwellReviewEndsAtRangeRule(16, 255),
            DwellReviewRequiredCarrierRule("dwell review needs a value"),
            DwellReviewTitleLengthRule(86400),
            DwellReviewPhoneLengthRule(1000),
        ),
    )
}
