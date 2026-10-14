package com.acme.transport.linehaul.trailer.bundle

/** A rule a lenient trailer bundle must pass. Check returns its problems, or none. */
interface LenientTrailerBundleRule {
    fun check(item: LenientTrailerBundle): MutableList<String>
}

data class LenientTrailerBundleRequiredCodeRule(var message: String) : LenientTrailerBundleRule {
    override fun check(item: LenientTrailerBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTrailerBundleRequiredNotesRule(var message: String) : LenientTrailerBundleRule {
    override fun check(item: LenientTrailerBundle): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTrailerBundleCreatedAtRangeRule(var min: Int, var max: Int) : LenientTrailerBundleRule {
    override fun check(item: LenientTrailerBundle): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class LenientTrailerBundleRetriesRangeRule(var min: Int, var max: Int) : LenientTrailerBundleRule {
    override fun check(item: LenientTrailerBundle): MutableList<String> {
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

/** Runs every rule of a lenient trailer bundle. */
data class LenientTrailerBundleValidator(var rules: MutableList<LenientTrailerBundleRule>) {
    fun validate(item: LenientTrailerBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientTrailerBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTrailerBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTrailerBundleValidator(): LenientTrailerBundleValidator {
    return LenientTrailerBundleValidator(
        mutableListOf<LenientTrailerBundleRule>(
            LenientTrailerBundleRequiredCodeRule("lenient trailer bundle needs an entry"),
            LenientTrailerBundleRequiredNotesRule("lenient trailer bundle needs a value"),
            LenientTrailerBundleCreatedAtRangeRule(16, 1000),
            LenientTrailerBundleRetriesRangeRule(255, 255),
        ),
    )
}
