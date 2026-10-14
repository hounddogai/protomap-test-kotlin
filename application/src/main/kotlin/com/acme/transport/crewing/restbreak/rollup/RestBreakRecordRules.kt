package com.acme.transport.crewing.restbreak.rollup

/** A rule a rest break record must pass. Check returns its problems, or none. */
interface RestBreakRecordRule {
    fun check(item: RestBreakRecord): MutableList<String>
}

data class RestBreakRecordLoadKgRangeRule(var min: Double, var max: Double) : RestBreakRecordRule {
    override fun check(item: RestBreakRecord): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRecordCodeLengthRule(var maxLength: Int) : RestBreakRecordRule {
    override fun check(item: RestBreakRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRecordSourceLengthRule(var maxLength: Int) : RestBreakRecordRule {
    override fun check(item: RestBreakRecord): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break record. */
data class RestBreakRecordValidator(var rules: MutableList<RestBreakRecordRule>) {
    fun validate(item: RestBreakRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakRecordValidator(): RestBreakRecordValidator {
    return RestBreakRecordValidator(
        mutableListOf<RestBreakRecordRule>(
            RestBreakRecordLoadKgRangeRule(7.4, 1062.1),
            RestBreakRecordCodeLengthRule(3600),
            RestBreakRecordSourceLengthRule(255),
        ),
    )
}
