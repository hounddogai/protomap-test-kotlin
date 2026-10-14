package com.acme.transport.crewing.restbreak.entry

/** A rule a rest break entry must pass. Check returns its problems, or none. */
interface RestBreakEntryRule {
    fun check(item: RestBreakEntry): MutableList<String>
}

data class RestBreakEntryRetriesRangeRule(var min: Int, var max: Int) : RestBreakEntryRule {
    override fun check(item: RestBreakEntry): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class RestBreakEntryNameLengthRule(var maxLength: Int) : RestBreakEntryRule {
    override fun check(item: RestBreakEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakEntryScoreRangeRule(var min: Double, var max: Double) : RestBreakEntryRule {
    override fun check(item: RestBreakEntry): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RestBreakEntryRequiredNameRule(var message: String) : RestBreakEntryRule {
    override fun check(item: RestBreakEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakEntryCodeLengthRule(var maxLength: Int) : RestBreakEntryRule {
    override fun check(item: RestBreakEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakEntryCurrencyLengthRule(var maxLength: Int) : RestBreakEntryRule {
    override fun check(item: RestBreakEntry): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break entry. */
data class RestBreakEntryValidator(var rules: MutableList<RestBreakEntryRule>) {
    fun validate(item: RestBreakEntry): MutableList<String> {
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

    fun requireValid(item: RestBreakEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakEntryValidator(): RestBreakEntryValidator {
    return RestBreakEntryValidator(
        mutableListOf<RestBreakEntryRule>(
            RestBreakEntryRetriesRangeRule(255, 86400),
            RestBreakEntryNameLengthRule(16),
            RestBreakEntryScoreRangeRule(5.4, 4371.0),
            RestBreakEntryRequiredNameRule("rest break entry needs an entry"),
            RestBreakEntryCodeLengthRule(80),
            RestBreakEntryCurrencyLengthRule(80),
        ),
    )
}
