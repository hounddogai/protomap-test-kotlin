package com.acme.transport.fleetops.serviceplan.notice

/** A rule a fallback service plan baseline must pass. Check returns its problems, or none. */
interface FallbackServicePlanBaselineRule {
    fun check(item: FallbackServicePlanBaseline): MutableList<String>
}

data class FallbackServicePlanBaselineSequenceRangeRule(
    var min: Int,
    var max: Int,
) : FallbackServicePlanBaselineRule {
    override fun check(item: FallbackServicePlanBaseline): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanBaselineRequiredExternalRefRule(
    var message: String,
) : FallbackServicePlanBaselineRule {
    override fun check(item: FallbackServicePlanBaseline): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanBaselineNameLengthRule(var maxLength: Int) : FallbackServicePlanBaselineRule {
    override fun check(item: FallbackServicePlanBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanBaselineExternalRefLengthRule(
    var maxLength: Int,
) : FallbackServicePlanBaselineRule {
    override fun check(item: FallbackServicePlanBaseline): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanBaselineCodeLengthRule(var maxLength: Int) : FallbackServicePlanBaselineRule {
    override fun check(item: FallbackServicePlanBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback service plan baseline. */
data class FallbackServicePlanBaselineValidator(var rules: MutableList<FallbackServicePlanBaselineRule>) {
    fun validate(item: FallbackServicePlanBaseline): MutableList<String> {
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

    fun requireValid(item: FallbackServicePlanBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackServicePlanBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackServicePlanBaselineValidator(): FallbackServicePlanBaselineValidator {
    return FallbackServicePlanBaselineValidator(
        mutableListOf<FallbackServicePlanBaselineRule>(
            FallbackServicePlanBaselineSequenceRangeRule(1000, 3600),
            FallbackServicePlanBaselineRequiredExternalRefRule("fallback service plan baseline needs an entry"),
            FallbackServicePlanBaselineNameLengthRule(120),
            FallbackServicePlanBaselineExternalRefLengthRule(32),
            FallbackServicePlanBaselineCodeLengthRule(120),
        ),
    )
}
