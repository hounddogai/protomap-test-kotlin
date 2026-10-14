package com.acme.transport.network.servicearea.areaassignment

/** A rule a scheduled service area notice must pass. Check returns its problems, or none. */
interface ScheduledServiceAreaNoticeRule {
    fun check(item: ScheduledServiceAreaNotice): MutableList<String>
}

data class ScheduledServiceAreaNoticeRequiredLocaleRule(var message: String) : ScheduledServiceAreaNoticeRule {
    override fun check(item: ScheduledServiceAreaNotice): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledServiceAreaNoticeLimitRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledServiceAreaNoticeRule {
    override fun check(item: ScheduledServiceAreaNotice): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class ScheduledServiceAreaNoticeWeightRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledServiceAreaNoticeRule {
    override fun check(item: ScheduledServiceAreaNotice): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class ScheduledServiceAreaNoticePinnedGuardRule(var limit: Double) : ScheduledServiceAreaNoticeRule {
    override fun check(item: ScheduledServiceAreaNotice): MutableList<String> {
        if (item.pinned && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled service area notice. */
data class ScheduledServiceAreaNoticeValidator(var rules: MutableList<ScheduledServiceAreaNoticeRule>) {
    fun validate(item: ScheduledServiceAreaNotice): MutableList<String> {
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

    fun requireValid(item: ScheduledServiceAreaNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledServiceAreaNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledServiceAreaNoticeValidator(): ScheduledServiceAreaNoticeValidator {
    return ScheduledServiceAreaNoticeValidator(
        mutableListOf<ScheduledServiceAreaNoticeRule>(
            ScheduledServiceAreaNoticeRequiredLocaleRule("scheduled service area notice needs an entry"),
            ScheduledServiceAreaNoticeLimitRangeRule(32, 3600),
            ScheduledServiceAreaNoticeWeightRangeRule(6.8, 3889.5),
            ScheduledServiceAreaNoticePinnedGuardRule(7.5),
        ),
    )
}
