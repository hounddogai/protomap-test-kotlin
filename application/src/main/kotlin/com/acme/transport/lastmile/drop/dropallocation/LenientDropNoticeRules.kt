package com.acme.transport.lastmile.drop.dropallocation

/** A rule a lenient drop notice must pass. Check returns its problems, or none. */
interface LenientDropNoticeRule {
    fun check(item: LenientDropNotice): MutableList<String>
}

data class LenientDropNoticeCodeLengthRule(var maxLength: Int) : LenientDropNoticeRule {
    override fun check(item: LenientDropNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropNoticeRequiredCategoryRule(var message: String) : LenientDropNoticeRule {
    override fun check(item: LenientDropNotice): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDropNoticeLabelLengthRule(var maxLength: Int) : LenientDropNoticeRule {
    override fun check(item: LenientDropNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient drop notice. */
data class LenientDropNoticeValidator(var rules: MutableList<LenientDropNoticeRule>) {
    fun validate(item: LenientDropNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDropNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDropNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDropNoticeValidator(): LenientDropNoticeValidator {
    return LenientDropNoticeValidator(
        mutableListOf<LenientDropNoticeRule>(
            LenientDropNoticeCodeLengthRule(120),
            LenientDropNoticeRequiredCategoryRule("lenient drop notice needs this field"),
            LenientDropNoticeLabelLengthRule(16),
        ),
    )
}
