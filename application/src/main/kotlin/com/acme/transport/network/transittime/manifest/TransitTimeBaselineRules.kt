package com.acme.transport.network.transittime.manifest

/** A rule a transit time baseline must pass. Check returns its problems, or none. */
interface TransitTimeBaselineRule {
    fun check(item: TransitTimeBaseline): MutableList<String>
}

data class TransitTimeBaselineRequiredChecksumRule(var message: String) : TransitTimeBaselineRule {
    override fun check(item: TransitTimeBaseline): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeBaselineCodeLengthRule(var maxLength: Int) : TransitTimeBaselineRule {
    override fun check(item: TransitTimeBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeBaselineChecksumLengthRule(var maxLength: Int) : TransitTimeBaselineRule {
    override fun check(item: TransitTimeBaseline): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeBaselineAttemptsRangeRule(var min: Int, var max: Int) : TransitTimeBaselineRule {
    override fun check(item: TransitTimeBaseline): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time baseline. */
data class TransitTimeBaselineValidator(var rules: MutableList<TransitTimeBaselineRule>) {
    fun validate(item: TransitTimeBaseline): MutableList<String> {
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

    fun requireValid(item: TransitTimeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeBaselineValidator(): TransitTimeBaselineValidator {
    return TransitTimeBaselineValidator(
        mutableListOf<TransitTimeBaselineRule>(
            TransitTimeBaselineRequiredChecksumRule("transit time baseline needs a value"),
            TransitTimeBaselineCodeLengthRule(3600),
            TransitTimeBaselineChecksumLengthRule(32),
            TransitTimeBaselineAttemptsRangeRule(64, 255),
        ),
    )
}
