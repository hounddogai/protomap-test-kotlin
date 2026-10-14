package com.acme.transport.freightpay.accessorial.accessorialadjustment

/** A rule a scheduled accessorial notice must pass. Check returns its problems, or none. */
interface ScheduledAccessorialNoticeRule {
    fun check(item: ScheduledAccessorialNotice): MutableList<String>
}

data class ScheduledAccessorialNoticeTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ScheduledAccessorialNoticeRule {
    override fun check(item: ScheduledAccessorialNotice): MutableList<String> {
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

data class ScheduledAccessorialNoticeRequiredChannelRule(var message: String) : ScheduledAccessorialNoticeRule {
    override fun check(item: ScheduledAccessorialNotice): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialNoticeCodeLengthRule(var maxLength: Int) : ScheduledAccessorialNoticeRule {
    override fun check(item: ScheduledAccessorialNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialNoticeChannelLengthRule(var maxLength: Int) : ScheduledAccessorialNoticeRule {
    override fun check(item: ScheduledAccessorialNotice): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled accessorial notice. */
data class ScheduledAccessorialNoticeValidator(var rules: MutableList<ScheduledAccessorialNoticeRule>) {
    fun validate(item: ScheduledAccessorialNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAccessorialNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAccessorialNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAccessorialNoticeValidator(): ScheduledAccessorialNoticeValidator {
    return ScheduledAccessorialNoticeValidator(
        mutableListOf<ScheduledAccessorialNoticeRule>(
            ScheduledAccessorialNoticeTagsLimitRule(16, mutableListOf<String>("manual", "default", "lenient")),
            ScheduledAccessorialNoticeRequiredChannelRule("scheduled accessorial notice needs a value"),
            ScheduledAccessorialNoticeCodeLengthRule(120),
            ScheduledAccessorialNoticeChannelLengthRule(64),
        ),
    )
}
