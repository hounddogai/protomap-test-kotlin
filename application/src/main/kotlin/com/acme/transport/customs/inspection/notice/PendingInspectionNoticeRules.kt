package com.acme.transport.customs.inspection.notice

/** A rule a pending inspection notice must pass. Check returns its problems, or none. */
interface PendingInspectionNoticeRule {
    fun check(item: PendingInspectionNotice): MutableList<String>
}

data class PendingInspectionNoticeRequiredCodeRule(var message: String) : PendingInspectionNoticeRule {
    override fun check(item: PendingInspectionNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionNoticeLocaleLengthRule(var maxLength: Int) : PendingInspectionNoticeRule {
    override fun check(item: PendingInspectionNotice): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionNoticeRateRangeRule(
    var min: Double,
    var max: Double,
) : PendingInspectionNoticeRule {
    override fun check(item: PendingInspectionNotice): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionNoticeRequiredLocaleRule(var message: String) : PendingInspectionNoticeRule {
    override fun check(item: PendingInspectionNotice): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionNoticeTitleLengthRule(var maxLength: Int) : PendingInspectionNoticeRule {
    override fun check(item: PendingInspectionNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection notice. */
data class PendingInspectionNoticeValidator(var rules: MutableList<PendingInspectionNoticeRule>) {
    fun validate(item: PendingInspectionNotice): MutableList<String> {
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

    fun requireValid(item: PendingInspectionNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionNoticeValidator(): PendingInspectionNoticeValidator {
    return PendingInspectionNoticeValidator(
        mutableListOf<PendingInspectionNoticeRule>(
            PendingInspectionNoticeRequiredCodeRule("pending inspection notice needs an entry"),
            PendingInspectionNoticeLocaleLengthRule(32),
            PendingInspectionNoticeRateRangeRule(4.4, 977.0),
            PendingInspectionNoticeRequiredLocaleRule("pending inspection notice needs this field"),
            PendingInspectionNoticeTitleLengthRule(3600),
        ),
    )
}
