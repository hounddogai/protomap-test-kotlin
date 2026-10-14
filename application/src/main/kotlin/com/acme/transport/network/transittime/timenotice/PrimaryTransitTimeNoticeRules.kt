package com.acme.transport.network.transittime.timenotice

/** A rule a primary transit time notice must pass. Check returns its problems, or none. */
interface PrimaryTransitTimeNoticeRule {
    fun check(item: PrimaryTransitTimeNotice): MutableList<String>
}

data class PrimaryTransitTimeNoticeSummaryLengthRule(var maxLength: Int) : PrimaryTransitTimeNoticeRule {
    override fun check(item: PrimaryTransitTimeNotice): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeNoticeCodeLengthRule(var maxLength: Int) : PrimaryTransitTimeNoticeRule {
    override fun check(item: PrimaryTransitTimeNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeNoticeRequiredCodeRule(var message: String) : PrimaryTransitTimeNoticeRule {
    override fun check(item: PrimaryTransitTimeNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTransitTimeNoticeChecksumLengthRule(var maxLength: Int) : PrimaryTransitTimeNoticeRule {
    override fun check(item: PrimaryTransitTimeNotice): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary transit time notice. */
data class PrimaryTransitTimeNoticeValidator(var rules: MutableList<PrimaryTransitTimeNoticeRule>) {
    fun validate(item: PrimaryTransitTimeNotice): MutableList<String> {
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

    fun requireValid(item: PrimaryTransitTimeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTransitTimeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTransitTimeNoticeValidator(): PrimaryTransitTimeNoticeValidator {
    return PrimaryTransitTimeNoticeValidator(
        mutableListOf<PrimaryTransitTimeNoticeRule>(
            PrimaryTransitTimeNoticeSummaryLengthRule(80),
            PrimaryTransitTimeNoticeCodeLengthRule(32),
            PrimaryTransitTimeNoticeRequiredCodeRule("primary transit time notice needs a value"),
            PrimaryTransitTimeNoticeChecksumLengthRule(1000),
        ),
    )
}
