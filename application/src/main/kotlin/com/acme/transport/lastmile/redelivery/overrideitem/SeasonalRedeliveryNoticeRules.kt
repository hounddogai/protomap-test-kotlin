package com.acme.transport.lastmile.redelivery.overrideitem

/** A rule a seasonal redelivery notice must pass. Check returns its problems, or none. */
interface SeasonalRedeliveryNoticeRule {
    fun check(item: SeasonalRedeliveryNotice): MutableList<String>
}

data class SeasonalRedeliveryNoticeTimezoneLengthRule(var maxLength: Int) : SeasonalRedeliveryNoticeRule {
    override fun check(item: SeasonalRedeliveryNotice): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryNoticeRequiredCodeRule(var message: String) : SeasonalRedeliveryNoticeRule {
    override fun check(item: SeasonalRedeliveryNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryNoticeApprovedGuardRule(var limit: Double) : SeasonalRedeliveryNoticeRule {
    override fun check(item: SeasonalRedeliveryNotice): MutableList<String> {
        if (item.approved && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryNoticeRequiredTitleRule(var message: String) : SeasonalRedeliveryNoticeRule {
    override fun check(item: SeasonalRedeliveryNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryNoticeCodeLengthRule(var maxLength: Int) : SeasonalRedeliveryNoticeRule {
    override fun check(item: SeasonalRedeliveryNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryNoticeTitleLengthRule(var maxLength: Int) : SeasonalRedeliveryNoticeRule {
    override fun check(item: SeasonalRedeliveryNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal redelivery notice. */
data class SeasonalRedeliveryNoticeValidator(var rules: MutableList<SeasonalRedeliveryNoticeRule>) {
    fun validate(item: SeasonalRedeliveryNotice): MutableList<String> {
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

    fun requireValid(item: SeasonalRedeliveryNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRedeliveryNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRedeliveryNoticeValidator(): SeasonalRedeliveryNoticeValidator {
    return SeasonalRedeliveryNoticeValidator(
        mutableListOf<SeasonalRedeliveryNoticeRule>(
            SeasonalRedeliveryNoticeTimezoneLengthRule(64),
            SeasonalRedeliveryNoticeRequiredCodeRule("seasonal redelivery notice needs this field"),
            SeasonalRedeliveryNoticeApprovedGuardRule(3.3),
            SeasonalRedeliveryNoticeRequiredTitleRule("seasonal redelivery notice needs a value"),
            SeasonalRedeliveryNoticeCodeLengthRule(32),
            SeasonalRedeliveryNoticeTitleLengthRule(255),
        ),
    )
}
