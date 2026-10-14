package com.acme.transport.linehaul.laneplan.window

/** A rule a fallback lane plan exception must pass. Check returns its problems, or none. */
interface FallbackLanePlanExceptionRule {
    fun check(item: FallbackLanePlanException): MutableList<String>
}

data class FallbackLanePlanExceptionCodeLengthRule(var maxLength: Int) : FallbackLanePlanExceptionRule {
    override fun check(item: FallbackLanePlanException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanExceptionCodePrefixRule(var prefix: String) : FallbackLanePlanExceptionRule {
    override fun check(item: FallbackLanePlanException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanExceptionSourceLengthRule(var maxLength: Int) : FallbackLanePlanExceptionRule {
    override fun check(item: FallbackLanePlanException): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanExceptionLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : FallbackLanePlanExceptionRule {
    override fun check(item: FallbackLanePlanException): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanExceptionVisibleGuardRule(var limit: Double) : FallbackLanePlanExceptionRule {
    override fun check(item: FallbackLanePlanException): MutableList<String> {
        if (item.visible && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanExceptionRequiredContactEmailRule(
    var message: String,
) : FallbackLanePlanExceptionRule {
    override fun check(item: FallbackLanePlanException): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback lane plan exception. */
data class FallbackLanePlanExceptionValidator(var rules: MutableList<FallbackLanePlanExceptionRule>) {
    fun validate(item: FallbackLanePlanException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackLanePlanException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLanePlanException>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLanePlanExceptionValidator(): FallbackLanePlanExceptionValidator {
    return FallbackLanePlanExceptionValidator(
        mutableListOf<FallbackLanePlanExceptionRule>(
            FallbackLanePlanExceptionCodeLengthRule(255),
            FallbackLanePlanExceptionCodePrefixRule("OPS"),
            FallbackLanePlanExceptionSourceLengthRule(64),
            FallbackLanePlanExceptionLoadKgRangeRule(5.4, 3857.6),
            FallbackLanePlanExceptionVisibleGuardRule(0.2),
            FallbackLanePlanExceptionRequiredContactEmailRule("fallback lane plan exception needs an entry"),
        ),
    )
}
