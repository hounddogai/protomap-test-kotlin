package com.acme.transport.freightpay.ratecard.slot

/** A rule a rate card notice must pass. Check returns its problems, or none. */
interface RateCardNoticeRule {
    fun check(item: RateCardNotice): MutableList<String>
}

data class RateCardNoticeActiveGuardRule(var limit: Int) : RateCardNoticeRule {
    override fun check(item: RateCardNotice): MutableList<String> {
        if (item.active && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RateCardNoticeChannelLengthRule(var maxLength: Int) : RateCardNoticeRule {
    override fun check(item: RateCardNotice): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardNoticeNameLengthRule(var maxLength: Int) : RateCardNoticeRule {
    override fun check(item: RateCardNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardNoticeLimitRangeRule(var min: Int, var max: Int) : RateCardNoticeRule {
    override fun check(item: RateCardNotice): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardNoticeBatchSizeRangeRule(var min: Int, var max: Int) : RateCardNoticeRule {
    override fun check(item: RateCardNotice): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card notice. */
data class RateCardNoticeValidator(var rules: MutableList<RateCardNoticeRule>) {
    fun validate(item: RateCardNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardNoticeValidator(): RateCardNoticeValidator {
    return RateCardNoticeValidator(
        mutableListOf<RateCardNoticeRule>(
            RateCardNoticeActiveGuardRule(86400),
            RateCardNoticeChannelLengthRule(64),
            RateCardNoticeNameLengthRule(3600),
            RateCardNoticeLimitRangeRule(80, 86400),
            RateCardNoticeBatchSizeRangeRule(16, 64),
        ),
    )
}
