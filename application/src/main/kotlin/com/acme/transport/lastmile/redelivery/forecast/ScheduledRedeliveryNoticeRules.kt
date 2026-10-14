package com.acme.transport.lastmile.redelivery.forecast

/** A rule a scheduled redelivery notice must pass. Check returns its problems, or none. */
interface ScheduledRedeliveryNoticeRule {
    fun check(item: ScheduledRedeliveryNotice): MutableList<String>
}

data class ScheduledRedeliveryNoticeCountryLengthRule(var maxLength: Int) : ScheduledRedeliveryNoticeRule {
    override fun check(item: ScheduledRedeliveryNotice): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryNoticeRequiredCodeRule(var message: String) : ScheduledRedeliveryNoticeRule {
    override fun check(item: ScheduledRedeliveryNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryNoticeRequiredCountryRule(var message: String) : ScheduledRedeliveryNoticeRule {
    override fun check(item: ScheduledRedeliveryNotice): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryNoticeDriverIdLengthRule(var maxLength: Int) : ScheduledRedeliveryNoticeRule {
    override fun check(item: ScheduledRedeliveryNotice): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled redelivery notice. */
data class ScheduledRedeliveryNoticeValidator(var rules: MutableList<ScheduledRedeliveryNoticeRule>) {
    fun validate(item: ScheduledRedeliveryNotice): MutableList<String> {
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

    fun requireValid(item: ScheduledRedeliveryNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRedeliveryNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRedeliveryNoticeValidator(): ScheduledRedeliveryNoticeValidator {
    return ScheduledRedeliveryNoticeValidator(
        mutableListOf<ScheduledRedeliveryNoticeRule>(
            ScheduledRedeliveryNoticeCountryLengthRule(255),
            ScheduledRedeliveryNoticeRequiredCodeRule("scheduled redelivery notice needs an entry"),
            ScheduledRedeliveryNoticeRequiredCountryRule("scheduled redelivery notice needs a value"),
            ScheduledRedeliveryNoticeDriverIdLengthRule(32),
        ),
    )
}
