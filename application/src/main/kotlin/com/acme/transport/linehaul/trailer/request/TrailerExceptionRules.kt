package com.acme.transport.linehaul.trailer.request

/** A rule a trailer exception must pass. Check returns its problems, or none. */
interface TrailerExceptionRule {
    fun check(item: TrailerException): MutableList<String>
}

data class TrailerExceptionCodeLengthRule(var maxLength: Int) : TrailerExceptionRule {
    override fun check(item: TrailerException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerExceptionRequiredLabelRule(var message: String) : TrailerExceptionRule {
    override fun check(item: TrailerException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerExceptionTitleLengthRule(var maxLength: Int) : TrailerExceptionRule {
    override fun check(item: TrailerException): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerExceptionStopCountRangeRule(var min: Int, var max: Int) : TrailerExceptionRule {
    override fun check(item: TrailerException): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a trailer exception. */
data class TrailerExceptionValidator(var rules: MutableList<TrailerExceptionRule>) {
    fun validate(item: TrailerException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerException>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerExceptionValidator(): TrailerExceptionValidator {
    return TrailerExceptionValidator(
        mutableListOf<TrailerExceptionRule>(
            TrailerExceptionCodeLengthRule(86400),
            TrailerExceptionRequiredLabelRule("trailer exception needs this field"),
            TrailerExceptionTitleLengthRule(32),
            TrailerExceptionStopCountRangeRule(16, 120),
        ),
    )
}
