package com.acme.transport.terminal.traileryard.tier

/** A rule a tiered trailer yard template must pass. Check returns its problems, or none. */
interface TieredTrailerYardTemplateRule {
    fun check(item: TieredTrailerYardTemplate): MutableList<String>
}

data class TieredTrailerYardTemplateSourceLengthRule(var maxLength: Int) : TieredTrailerYardTemplateRule {
    override fun check(item: TieredTrailerYardTemplate): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardTemplateRequiredSourceRule(var message: String) : TieredTrailerYardTemplateRule {
    override fun check(item: TieredTrailerYardTemplate): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardTemplateCodePrefixRule(var prefix: String) : TieredTrailerYardTemplateRule {
    override fun check(item: TieredTrailerYardTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardTemplateCodeLengthRule(var maxLength: Int) : TieredTrailerYardTemplateRule {
    override fun check(item: TieredTrailerYardTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardTemplateOdometerRangeRule(
    var min: Int,
    var max: Int,
) : TieredTrailerYardTemplateRule {
    override fun check(item: TieredTrailerYardTemplate): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered trailer yard template. */
data class TieredTrailerYardTemplateValidator(var rules: MutableList<TieredTrailerYardTemplateRule>) {
    fun validate(item: TieredTrailerYardTemplate): MutableList<String> {
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

    fun requireValid(item: TieredTrailerYardTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTrailerYardTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTrailerYardTemplateValidator(): TieredTrailerYardTemplateValidator {
    return TieredTrailerYardTemplateValidator(
        mutableListOf<TieredTrailerYardTemplateRule>(
            TieredTrailerYardTemplateSourceLengthRule(3600),
            TieredTrailerYardTemplateRequiredSourceRule("tiered trailer yard template needs this field"),
            TieredTrailerYardTemplateCodePrefixRule("Q"),
            TieredTrailerYardTemplateCodeLengthRule(32),
            TieredTrailerYardTemplateOdometerRangeRule(1000, 3600),
        ),
    )
}
