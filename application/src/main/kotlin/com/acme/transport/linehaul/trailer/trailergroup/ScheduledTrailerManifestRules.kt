package com.acme.transport.linehaul.trailer.trailergroup

/** A rule a scheduled trailer manifest must pass. Check returns its problems, or none. */
interface ScheduledTrailerManifestRule {
    fun check(item: ScheduledTrailerManifest): MutableList<String>
}

data class ScheduledTrailerManifestActiveGuardRule(var limit: Int) : ScheduledTrailerManifestRule {
    override fun check(item: ScheduledTrailerManifest): MutableList<String> {
        if (item.active && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerManifestRequiredTitleRule(var message: String) : ScheduledTrailerManifestRule {
    override fun check(item: ScheduledTrailerManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerManifestTitleLengthRule(var maxLength: Int) : ScheduledTrailerManifestRule {
    override fun check(item: ScheduledTrailerManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerManifestLimitRangeRule(var min: Int, var max: Int) : ScheduledTrailerManifestRule {
    override fun check(item: ScheduledTrailerManifest): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled trailer manifest. */
data class ScheduledTrailerManifestValidator(var rules: MutableList<ScheduledTrailerManifestRule>) {
    fun validate(item: ScheduledTrailerManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledTrailerManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTrailerManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTrailerManifestValidator(): ScheduledTrailerManifestValidator {
    return ScheduledTrailerManifestValidator(
        mutableListOf<ScheduledTrailerManifestRule>(
            ScheduledTrailerManifestActiveGuardRule(86400),
            ScheduledTrailerManifestRequiredTitleRule("scheduled trailer manifest needs a value"),
            ScheduledTrailerManifestTitleLengthRule(120),
            ScheduledTrailerManifestLimitRangeRule(64, 64),
        ),
    )
}
