package com.acme.transport.freightpay.ratecard.segment

/** A rule a capped rate card notice must pass. Check returns its problems, or none. */
interface CappedRateCardNoticeRule {
    fun check(item: CappedRateCardNotice): MutableList<String>
}

data class CappedRateCardNoticeTitleLengthRule(var maxLength: Int) : CappedRateCardNoticeRule {
    override fun check(item: CappedRateCardNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardNoticeCodeLengthRule(var maxLength: Int) : CappedRateCardNoticeRule {
    override fun check(item: CappedRateCardNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardNoticeRequiredTrailerIdRule(var message: String) : CappedRateCardNoticeRule {
    override fun check(item: CappedRateCardNotice): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardNoticeTrailerIdLengthRule(var maxLength: Int) : CappedRateCardNoticeRule {
    override fun check(item: CappedRateCardNotice): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardNoticePinnedGuardRule(var limit: Double) : CappedRateCardNoticeRule {
    override fun check(item: CappedRateCardNotice): MutableList<String> {
        if (item.pinned && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardNoticeRequiredLabelRule(var message: String) : CappedRateCardNoticeRule {
    override fun check(item: CappedRateCardNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped rate card notice. */
data class CappedRateCardNoticeValidator(var rules: MutableList<CappedRateCardNoticeRule>) {
    fun validate(item: CappedRateCardNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRateCardNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRateCardNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRateCardNoticeValidator(): CappedRateCardNoticeValidator {
    return CappedRateCardNoticeValidator(
        mutableListOf<CappedRateCardNoticeRule>(
            CappedRateCardNoticeTitleLengthRule(120),
            CappedRateCardNoticeCodeLengthRule(32),
            CappedRateCardNoticeRequiredTrailerIdRule("capped rate card notice needs a value"),
            CappedRateCardNoticeTrailerIdLengthRule(80),
            CappedRateCardNoticePinnedGuardRule(4.5),
            CappedRateCardNoticeRequiredLabelRule("capped rate card notice needs this field"),
        ),
    )
}
