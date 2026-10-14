package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.normalize

/** A rule a tender exception must pass. Check returns its problems, or none. */
interface TenderExceptionRule {
    fun check(item: TenderException): MutableList<String>
}

data class TenderExceptionRequiredNameRule(var message: String) : TenderExceptionRule {
    override fun check(item: TenderException): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderExceptionAllowedSegmentRule(var allowed: MutableList<String>) : TenderExceptionRule {
    override fun check(item: TenderException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is stale")
        }
        return mutableListOf<String>()
    }
}

data class TenderExceptionStopCountRangeRule(var min: Int, var max: Int) : TenderExceptionRule {
    override fun check(item: TenderException): MutableList<String> {
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

data class TenderExceptionOdometerRangeRule(var min: Int, var max: Int) : TenderExceptionRule {
    override fun check(item: TenderException): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class TenderExceptionCodeLengthRule(var maxLength: Int) : TenderExceptionRule {
    override fun check(item: TenderException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender exception. */
data class TenderExceptionValidator(var rules: MutableList<TenderExceptionRule>) {
    fun validate(item: TenderException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderException>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderExceptionValidator(): TenderExceptionValidator {
    return TenderExceptionValidator(
        mutableListOf<TenderExceptionRule>(
            TenderExceptionRequiredNameRule("tender exception needs a value"),
            TenderExceptionAllowedSegmentRule(mutableListOf<String>("lenient")),
            TenderExceptionStopCountRangeRule(16, 64),
            TenderExceptionOdometerRangeRule(120, 255),
            TenderExceptionCodeLengthRule(120),
        ),
    )
}
