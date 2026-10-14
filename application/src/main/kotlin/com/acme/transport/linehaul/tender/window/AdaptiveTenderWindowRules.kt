package com.acme.transport.linehaul.tender.window

/** A rule a adaptive tender window must pass. Check returns its problems, or none. */
interface AdaptiveTenderWindowRule {
    fun check(item: AdaptiveTenderWindow): MutableList<String>
}

data class AdaptiveTenderWindowOdometerRangeRule(var min: Int, var max: Int) : AdaptiveTenderWindowRule {
    override fun check(item: AdaptiveTenderWindow): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderWindowRequiredSummaryRule(var message: String) : AdaptiveTenderWindowRule {
    override fun check(item: AdaptiveTenderWindow): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderWindowCodeLengthRule(var maxLength: Int) : AdaptiveTenderWindowRule {
    override fun check(item: AdaptiveTenderWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderWindowSummaryLengthRule(var maxLength: Int) : AdaptiveTenderWindowRule {
    override fun check(item: AdaptiveTenderWindow): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderWindowTitleLengthRule(var maxLength: Int) : AdaptiveTenderWindowRule {
    override fun check(item: AdaptiveTenderWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive tender window. */
data class AdaptiveTenderWindowValidator(var rules: MutableList<AdaptiveTenderWindowRule>) {
    fun validate(item: AdaptiveTenderWindow): MutableList<String> {
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

    fun requireValid(item: AdaptiveTenderWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTenderWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTenderWindowValidator(): AdaptiveTenderWindowValidator {
    return AdaptiveTenderWindowValidator(
        mutableListOf<AdaptiveTenderWindowRule>(
            AdaptiveTenderWindowOdometerRangeRule(64, 120),
            AdaptiveTenderWindowRequiredSummaryRule("adaptive tender window needs a value"),
            AdaptiveTenderWindowCodeLengthRule(3600),
            AdaptiveTenderWindowSummaryLengthRule(80),
            AdaptiveTenderWindowTitleLengthRule(16),
        ),
    )
}
