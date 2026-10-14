package com.acme.transport.linehaul.trailer.group

/** A rule a trailer mapping must pass. Check returns its problems, or none. */
interface TrailerMappingRule {
    fun check(item: TrailerMapping): MutableList<String>
}

data class TrailerMappingVersionRangeRule(var min: Int, var max: Int) : TrailerMappingRule {
    override fun check(item: TrailerMapping): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerMappingTitleLengthRule(var maxLength: Int) : TrailerMappingRule {
    override fun check(item: TrailerMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerMappingCodeLengthRule(var maxLength: Int) : TrailerMappingRule {
    override fun check(item: TrailerMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer mapping. */
data class TrailerMappingValidator(var rules: MutableList<TrailerMappingRule>) {
    fun validate(item: TrailerMapping): MutableList<String> {
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

    fun requireValid(item: TrailerMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerMappingValidator(): TrailerMappingValidator {
    return TrailerMappingValidator(
        mutableListOf<TrailerMappingRule>(
            TrailerMappingVersionRangeRule(80, 80),
            TrailerMappingTitleLengthRule(1000),
            TrailerMappingCodeLengthRule(120),
        ),
    )
}
