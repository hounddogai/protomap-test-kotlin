package com.acme.transport.crewing.restbreak.overrideitem

/** A rule a legacy rest break mapping must pass. Check returns its problems, or none. */
interface LegacyRestBreakMappingRule {
    fun check(item: LegacyRestBreakMapping): MutableList<String>
}

data class LegacyRestBreakMappingCodeLengthRule(var maxLength: Int) : LegacyRestBreakMappingRule {
    override fun check(item: LegacyRestBreakMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakMappingBatchSizeRangeRule(var min: Int, var max: Int) : LegacyRestBreakMappingRule {
    override fun check(item: LegacyRestBreakMapping): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class LegacyRestBreakMappingRatioRangeRule(var min: Double, var max: Double) : LegacyRestBreakMappingRule {
    override fun check(item: LegacyRestBreakMapping): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakMappingRequiredNameRule(var message: String) : LegacyRestBreakMappingRule {
    override fun check(item: LegacyRestBreakMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy rest break mapping. */
data class LegacyRestBreakMappingValidator(var rules: MutableList<LegacyRestBreakMappingRule>) {
    fun validate(item: LegacyRestBreakMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyRestBreakMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRestBreakMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRestBreakMappingValidator(): LegacyRestBreakMappingValidator {
    return LegacyRestBreakMappingValidator(
        mutableListOf<LegacyRestBreakMappingRule>(
            LegacyRestBreakMappingCodeLengthRule(255),
            LegacyRestBreakMappingBatchSizeRangeRule(16, 3600),
            LegacyRestBreakMappingRatioRangeRule(7.1, 1517.7),
            LegacyRestBreakMappingRequiredNameRule("legacy rest break mapping needs an entry"),
        ),
    )
}
