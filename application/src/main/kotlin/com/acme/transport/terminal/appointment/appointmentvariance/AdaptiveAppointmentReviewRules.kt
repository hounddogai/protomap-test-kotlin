package com.acme.transport.terminal.appointment.appointmentvariance

/** A rule a adaptive appointment review must pass. Check returns its problems, or none. */
interface AdaptiveAppointmentReviewRule {
    fun check(item: AdaptiveAppointmentReview): MutableList<String>
}

data class AdaptiveAppointmentReviewRequiredCodeRule(var message: String) : AdaptiveAppointmentReviewRule {
    override fun check(item: AdaptiveAppointmentReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentReviewRequiredTitleRule(var message: String) : AdaptiveAppointmentReviewRule {
    override fun check(item: AdaptiveAppointmentReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentReviewTitleLengthRule(var maxLength: Int) : AdaptiveAppointmentReviewRule {
    override fun check(item: AdaptiveAppointmentReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentReviewExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveAppointmentReviewRule {
    override fun check(item: AdaptiveAppointmentReview): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveAppointmentReviewCodeLengthRule(var maxLength: Int) : AdaptiveAppointmentReviewRule {
    override fun check(item: AdaptiveAppointmentReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentReviewCodePrefixRule(var prefix: String) : AdaptiveAppointmentReviewRule {
    override fun check(item: AdaptiveAppointmentReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive appointment review. */
data class AdaptiveAppointmentReviewValidator(var rules: MutableList<AdaptiveAppointmentReviewRule>) {
    fun validate(item: AdaptiveAppointmentReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAppointmentReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAppointmentReview>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAppointmentReviewValidator(): AdaptiveAppointmentReviewValidator {
    return AdaptiveAppointmentReviewValidator(
        mutableListOf<AdaptiveAppointmentReviewRule>(
            AdaptiveAppointmentReviewRequiredCodeRule("adaptive appointment review needs a value"),
            AdaptiveAppointmentReviewRequiredTitleRule("adaptive appointment review needs an entry"),
            AdaptiveAppointmentReviewTitleLengthRule(120),
            AdaptiveAppointmentReviewExpiresAtRangeRule(86400, 86400),
            AdaptiveAppointmentReviewCodeLengthRule(16),
            AdaptiveAppointmentReviewCodePrefixRule("REF"),
        ),
    )
}
