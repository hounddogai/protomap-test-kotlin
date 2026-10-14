package com.acme.transport.terminal.dwell.notice

/** A rule a dwell notice must pass. Check returns its problems, or none. */
interface DwellNoticeRule {
    fun check(item: DwellNotice): MutableList<String>
}

data class DwellNoticeNotesLengthRule(var maxLength: Int) : DwellNoticeRule {
    override fun check(item: DwellNotice): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellNoticeRequiredCodeRule(var message: String) : DwellNoticeRule {
    override fun check(item: DwellNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellNoticeLimitRangeRule(var min: Int, var max: Int) : DwellNoticeRule {
    override fun check(item: DwellNotice): MutableList<String> {
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

data class DwellNoticeRequiredTitleRule(var message: String) : DwellNoticeRule {
    override fun check(item: DwellNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell notice. */
data class DwellNoticeValidator(var rules: MutableList<DwellNoticeRule>) {
    fun validate(item: DwellNotice): MutableList<String> {
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

    fun requireValid(item: DwellNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellNoticeValidator(): DwellNoticeValidator {
    return DwellNoticeValidator(
        mutableListOf<DwellNoticeRule>(
            DwellNoticeNotesLengthRule(80),
            DwellNoticeRequiredCodeRule("dwell notice needs this field"),
            DwellNoticeLimitRangeRule(32, 64),
            DwellNoticeRequiredTitleRule("dwell notice needs a value"),
        ),
    )
}
