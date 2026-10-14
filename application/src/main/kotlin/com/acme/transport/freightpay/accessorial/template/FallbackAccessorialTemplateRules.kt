package com.acme.transport.freightpay.accessorial.template

/** A rule a fallback accessorial template must pass. Check returns its problems, or none. */
interface FallbackAccessorialTemplateRule {
    fun check(item: FallbackAccessorialTemplate): MutableList<String>
}

data class FallbackAccessorialTemplateRequiredCodeRule(var message: String) : FallbackAccessorialTemplateRule {
    override fun check(item: FallbackAccessorialTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialTemplateLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : FallbackAccessorialTemplateRule {
    override fun check(item: FallbackAccessorialTemplate): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class FallbackAccessorialTemplateExternalRefLengthRule(
    var maxLength: Int,
) : FallbackAccessorialTemplateRule {
    override fun check(item: FallbackAccessorialTemplate): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialTemplateRequiredSummaryRule(
    var message: String,
) : FallbackAccessorialTemplateRule {
    override fun check(item: FallbackAccessorialTemplate): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAccessorialTemplateCodeLengthRule(var maxLength: Int) : FallbackAccessorialTemplateRule {
    override fun check(item: FallbackAccessorialTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback accessorial template. */
data class FallbackAccessorialTemplateValidator(var rules: MutableList<FallbackAccessorialTemplateRule>) {
    fun validate(item: FallbackAccessorialTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackAccessorialTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAccessorialTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAccessorialTemplateValidator(): FallbackAccessorialTemplateValidator {
    return FallbackAccessorialTemplateValidator(
        mutableListOf<FallbackAccessorialTemplateRule>(
            FallbackAccessorialTemplateRequiredCodeRule("fallback accessorial template needs an entry"),
            FallbackAccessorialTemplateLoadKgRangeRule(2.5, 576.5),
            FallbackAccessorialTemplateExternalRefLengthRule(3600),
            FallbackAccessorialTemplateRequiredSummaryRule("fallback accessorial template needs an entry"),
            FallbackAccessorialTemplateCodeLengthRule(1000),
        ),
    )
}
