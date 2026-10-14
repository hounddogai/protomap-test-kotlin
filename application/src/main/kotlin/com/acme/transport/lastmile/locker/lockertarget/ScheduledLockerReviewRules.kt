package com.acme.transport.lastmile.locker.lockertarget

/** A rule a scheduled locker review must pass. Check returns its problems, or none. */
interface ScheduledLockerReviewRule {
    fun check(item: ScheduledLockerReview): MutableList<String>
}

data class ScheduledLockerReviewRequiredPhoneRule(var message: String) : ScheduledLockerReviewRule {
    override fun check(item: ScheduledLockerReview): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerReviewDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledLockerReviewRule {
    override fun check(item: ScheduledLockerReview): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerReviewRequiredNameRule(var message: String) : ScheduledLockerReviewRule {
    override fun check(item: ScheduledLockerReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled locker review. */
data class ScheduledLockerReviewValidator(var rules: MutableList<ScheduledLockerReviewRule>) {
    fun validate(item: ScheduledLockerReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledLockerReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLockerReview>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLockerReviewValidator(): ScheduledLockerReviewValidator {
    return ScheduledLockerReviewValidator(
        mutableListOf<ScheduledLockerReviewRule>(
            ScheduledLockerReviewRequiredPhoneRule("scheduled locker review needs this field"),
            ScheduledLockerReviewDurationMinutesRangeRule(120, 3600),
            ScheduledLockerReviewRequiredNameRule("scheduled locker review needs a value"),
        ),
    )
}
