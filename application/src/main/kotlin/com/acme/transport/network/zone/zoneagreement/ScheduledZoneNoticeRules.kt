package com.acme.transport.network.zone.zoneagreement

/** A rule a scheduled zone notice must pass. Check returns its problems, or none. */
interface ScheduledZoneNoticeRule {
    fun check(item: ScheduledZoneNotice): MutableList<String>
}

data class ScheduledZoneNoticeEtaMinutesRangeRule(var min: Int, var max: Int) : ScheduledZoneNoticeRule {
    override fun check(item: ScheduledZoneNotice): MutableList<String> {
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

data class ScheduledZoneNoticeRequiredCodeRule(var message: String) : ScheduledZoneNoticeRule {
    override fun check(item: ScheduledZoneNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneNoticeRequiredNameRule(var message: String) : ScheduledZoneNoticeRule {
    override fun check(item: ScheduledZoneNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled zone notice. */
data class ScheduledZoneNoticeValidator(var rules: MutableList<ScheduledZoneNoticeRule>) {
    fun validate(item: ScheduledZoneNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledZoneNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledZoneNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledZoneNoticeValidator(): ScheduledZoneNoticeValidator {
    return ScheduledZoneNoticeValidator(
        mutableListOf<ScheduledZoneNoticeRule>(
            ScheduledZoneNoticeEtaMinutesRangeRule(64, 64),
            ScheduledZoneNoticeRequiredCodeRule("scheduled zone notice needs a value"),
            ScheduledZoneNoticeRequiredNameRule("scheduled zone notice needs this field"),
        ),
    )
}
