package com.acme.transport.network.transittime.forecast

/** A rule a fallback transit time template must pass. Check returns its problems, or none. */
interface FallbackTransitTimeTemplateRule {
    fun check(item: FallbackTransitTimeTemplate): MutableList<String>
}

data class FallbackTransitTimeTemplateLimitRangeRule(
    var min: Int,
    var max: Int,
) : FallbackTransitTimeTemplateRule {
    override fun check(item: FallbackTransitTimeTemplate): MutableList<String> {
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

data class FallbackTransitTimeTemplateCodePrefixRule(var prefix: String) : FallbackTransitTimeTemplateRule {
    override fun check(item: FallbackTransitTimeTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeTemplateFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : FallbackTransitTimeTemplateRule {
    override fun check(item: FallbackTransitTimeTemplate): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class FallbackTransitTimeTemplateRequiredCodeRule(var message: String) : FallbackTransitTimeTemplateRule {
    override fun check(item: FallbackTransitTimeTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeTemplateRequiredNameRule(var message: String) : FallbackTransitTimeTemplateRule {
    override fun check(item: FallbackTransitTimeTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeTemplateVersionRangeRule(
    var min: Int,
    var max: Int,
) : FallbackTransitTimeTemplateRule {
    override fun check(item: FallbackTransitTimeTemplate): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback transit time template. */
data class FallbackTransitTimeTemplateValidator(var rules: MutableList<FallbackTransitTimeTemplateRule>) {
    fun validate(item: FallbackTransitTimeTemplate): MutableList<String> {
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

    fun requireValid(item: FallbackTransitTimeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTransitTimeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTransitTimeTemplateValidator(): FallbackTransitTimeTemplateValidator {
    return FallbackTransitTimeTemplateValidator(
        mutableListOf<FallbackTransitTimeTemplateRule>(
            FallbackTransitTimeTemplateLimitRangeRule(32, 64),
            FallbackTransitTimeTemplateCodePrefixRule("REF"),
            FallbackTransitTimeTemplateFuelLitersRangeRule(2.9, 3620.2),
            FallbackTransitTimeTemplateRequiredCodeRule("fallback transit time template needs a value"),
            FallbackTransitTimeTemplateRequiredNameRule("fallback transit time template needs a value"),
            FallbackTransitTimeTemplateVersionRangeRule(16, 64),
        ),
    )
}
