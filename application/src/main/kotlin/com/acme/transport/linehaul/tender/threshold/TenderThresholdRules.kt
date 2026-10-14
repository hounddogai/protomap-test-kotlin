package com.acme.transport.linehaul.tender.threshold

/** A rule a tender threshold must pass. Check returns its problems, or none. */
interface TenderThresholdRule {
    fun check(item: TenderThreshold): MutableList<String>
}

data class TenderThresholdCodePrefixRule(var prefix: String) : TenderThresholdRule {
    override fun check(item: TenderThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderThresholdCeilingRangeRule(var min: Double, var max: Double) : TenderThresholdRule {
    override fun check(item: TenderThreshold): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderThresholdChecksumLengthRule(var maxLength: Int) : TenderThresholdRule {
    override fun check(item: TenderThreshold): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderThresholdCodeLengthRule(var maxLength: Int) : TenderThresholdRule {
    override fun check(item: TenderThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderThresholdLimitRangeRule(var min: Int, var max: Int) : TenderThresholdRule {
    override fun check(item: TenderThreshold): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tender threshold. */
data class TenderThresholdValidator(var rules: MutableList<TenderThresholdRule>) {
    fun validate(item: TenderThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderThresholdValidator(): TenderThresholdValidator {
    return TenderThresholdValidator(
        mutableListOf<TenderThresholdRule>(
            TenderThresholdCodePrefixRule("OPS"),
            TenderThresholdCeilingRangeRule(1.0, 427.9),
            TenderThresholdChecksumLengthRule(64),
            TenderThresholdCodeLengthRule(86400),
            TenderThresholdLimitRangeRule(64, 80),
        ),
    )
}
