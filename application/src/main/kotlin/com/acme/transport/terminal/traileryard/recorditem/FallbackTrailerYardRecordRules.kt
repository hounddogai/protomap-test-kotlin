package com.acme.transport.terminal.traileryard.recorditem

/** A rule a fallback trailer yard record must pass. Check returns its problems, or none. */
interface FallbackTrailerYardRecordRule {
    fun check(item: FallbackTrailerYardRecord): MutableList<String>
}

data class FallbackTrailerYardRecordLabelLengthRule(var maxLength: Int) : FallbackTrailerYardRecordRule {
    override fun check(item: FallbackTrailerYardRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardRecordExternalRefLengthRule(var maxLength: Int) : FallbackTrailerYardRecordRule {
    override fun check(item: FallbackTrailerYardRecord): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardRecordCodeLengthRule(var maxLength: Int) : FallbackTrailerYardRecordRule {
    override fun check(item: FallbackTrailerYardRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardRecordRequiredExternalRefRule(
    var message: String,
) : FallbackTrailerYardRecordRule {
    override fun check(item: FallbackTrailerYardRecord): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardRecordSequenceRangeRule(
    var min: Int,
    var max: Int,
) : FallbackTrailerYardRecordRule {
    override fun check(item: FallbackTrailerYardRecord): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback trailer yard record. */
data class FallbackTrailerYardRecordValidator(var rules: MutableList<FallbackTrailerYardRecordRule>) {
    fun validate(item: FallbackTrailerYardRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackTrailerYardRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTrailerYardRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTrailerYardRecordValidator(): FallbackTrailerYardRecordValidator {
    return FallbackTrailerYardRecordValidator(
        mutableListOf<FallbackTrailerYardRecordRule>(
            FallbackTrailerYardRecordLabelLengthRule(3600),
            FallbackTrailerYardRecordExternalRefLengthRule(32),
            FallbackTrailerYardRecordCodeLengthRule(86400),
            FallbackTrailerYardRecordRequiredExternalRefRule("fallback trailer yard record needs this field"),
            FallbackTrailerYardRecordSequenceRangeRule(64, 3600),
        ),
    )
}
