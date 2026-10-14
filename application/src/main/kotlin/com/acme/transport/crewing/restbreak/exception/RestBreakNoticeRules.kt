package com.acme.transport.crewing.restbreak.exception

/** A rule a rest break notice must pass. Check returns its problems, or none. */
interface RestBreakNoticeRule {
    fun check(item: RestBreakNotice): MutableList<String>
}

data class RestBreakNoticeRequiredCodeRule(var message: String) : RestBreakNoticeRule {
    override fun check(item: RestBreakNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakNoticeNameLengthRule(var maxLength: Int) : RestBreakNoticeRule {
    override fun check(item: RestBreakNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakNoticeWindowDaysRangeRule(var min: Int, var max: Int) : RestBreakNoticeRule {
    override fun check(item: RestBreakNotice): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class RestBreakNoticeRevisionRangeRule(var min: Int, var max: Int) : RestBreakNoticeRule {
    override fun check(item: RestBreakNotice): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakNoticeCodeLengthRule(var maxLength: Int) : RestBreakNoticeRule {
    override fun check(item: RestBreakNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakNoticeCodePrefixRule(var prefix: String) : RestBreakNoticeRule {
    override fun check(item: RestBreakNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break notice. */
data class RestBreakNoticeValidator(var rules: MutableList<RestBreakNoticeRule>) {
    fun validate(item: RestBreakNotice): MutableList<String> {
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

    fun requireValid(item: RestBreakNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakNoticeValidator(): RestBreakNoticeValidator {
    return RestBreakNoticeValidator(
        mutableListOf<RestBreakNoticeRule>(
            RestBreakNoticeRequiredCodeRule("rest break notice needs an entry"),
            RestBreakNoticeNameLengthRule(86400),
            RestBreakNoticeWindowDaysRangeRule(120, 3600),
            RestBreakNoticeRevisionRangeRule(64, 80),
            RestBreakNoticeCodeLengthRule(80),
            RestBreakNoticeCodePrefixRule("ACM"),
        ),
    )
}
