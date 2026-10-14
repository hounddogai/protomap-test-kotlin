package com.acme.transport.linehaul.trailer.trailerrule

/** A rule a trailer notice must pass. Check returns its problems, or none. */
interface TrailerNoticeRule {
    fun check(item: TrailerNotice): MutableList<String>
}

data class TrailerNoticeDockLengthRule(var maxLength: Int) : TrailerNoticeRule {
    override fun check(item: TrailerNotice): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerNoticeLocaleLengthRule(var maxLength: Int) : TrailerNoticeRule {
    override fun check(item: TrailerNotice): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerNoticeRequiredLocaleRule(var message: String) : TrailerNoticeRule {
    override fun check(item: TrailerNotice): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerNoticeRequiredNameRule(var message: String) : TrailerNoticeRule {
    override fun check(item: TrailerNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerNoticeRequiredDockRule(var message: String) : TrailerNoticeRule {
    override fun check(item: TrailerNotice): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerNoticeRequiredCodeRule(var message: String) : TrailerNoticeRule {
    override fun check(item: TrailerNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer notice. */
data class TrailerNoticeValidator(var rules: MutableList<TrailerNoticeRule>) {
    fun validate(item: TrailerNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerNoticeValidator(): TrailerNoticeValidator {
    return TrailerNoticeValidator(
        mutableListOf<TrailerNoticeRule>(
            TrailerNoticeDockLengthRule(1000),
            TrailerNoticeLocaleLengthRule(120),
            TrailerNoticeRequiredLocaleRule("trailer notice needs this field"),
            TrailerNoticeRequiredNameRule("trailer notice needs this field"),
            TrailerNoticeRequiredDockRule("trailer notice needs an entry"),
            TrailerNoticeRequiredCodeRule("trailer notice needs a value"),
        ),
    )
}
