package com.acme.transport.terminal.appointment.score

/** A rule a adaptive appointment window must pass. Check returns its problems, or none. */
interface AdaptiveAppointmentWindowRule {
    fun check(item: AdaptiveAppointmentWindow): MutableList<String>
}

data class AdaptiveAppointmentWindowTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : AdaptiveAppointmentWindowRule {
    override fun check(item: AdaptiveAppointmentWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class AdaptiveAppointmentWindowCategoryLengthRule(var maxLength: Int) : AdaptiveAppointmentWindowRule {
    override fun check(item: AdaptiveAppointmentWindow): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentWindowActiveGuardRule(var limit: Double) : AdaptiveAppointmentWindowRule {
    override fun check(item: AdaptiveAppointmentWindow): MutableList<String> {
        if (item.active && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentWindowCodePrefixRule(var prefix: String) : AdaptiveAppointmentWindowRule {
    override fun check(item: AdaptiveAppointmentWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive appointment window. */
data class AdaptiveAppointmentWindowValidator(var rules: MutableList<AdaptiveAppointmentWindowRule>) {
    fun validate(item: AdaptiveAppointmentWindow): MutableList<String> {
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

    fun requireValid(item: AdaptiveAppointmentWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAppointmentWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAppointmentWindowValidator(): AdaptiveAppointmentWindowValidator {
    return AdaptiveAppointmentWindowValidator(
        mutableListOf<AdaptiveAppointmentWindowRule>(
            AdaptiveAppointmentWindowTagsLimitRule(1000, mutableListOf<String>("scheduled")),
            AdaptiveAppointmentWindowCategoryLengthRule(16),
            AdaptiveAppointmentWindowActiveGuardRule(0.5),
            AdaptiveAppointmentWindowCodePrefixRule("Q"),
        ),
    )
}
