package com.acme.transport.terminal.traileryard.allocation

/** A rule a trailer yard rule must pass. Check returns its problems, or none. */
interface TrailerYardRuleRule {
    fun check(item: TrailerYardRule): MutableList<String>
}

data class TrailerYardRuleSourceLengthRule(var maxLength: Int) : TrailerYardRuleRule {
    override fun check(item: TrailerYardRule): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardRuleRequiredSourceRule(var message: String) : TrailerYardRuleRule {
    override fun check(item: TrailerYardRule): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardRuleRequiredLabelRule(var message: String) : TrailerYardRuleRule {
    override fun check(item: TrailerYardRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardRuleCodePrefixRule(var prefix: String) : TrailerYardRuleRule {
    override fun check(item: TrailerYardRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardRuleMarginRangeRule(var min: Double, var max: Double) : TrailerYardRuleRule {
    override fun check(item: TrailerYardRule): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardRuleEtaMinutesRangeRule(var min: Int, var max: Int) : TrailerYardRuleRule {
    override fun check(item: TrailerYardRule): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a trailer yard rule. */
data class TrailerYardRuleValidator(var rules: MutableList<TrailerYardRuleRule>) {
    fun validate(item: TrailerYardRule): MutableList<String> {
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

    fun requireValid(item: TrailerYardRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardRuleValidator(): TrailerYardRuleValidator {
    return TrailerYardRuleValidator(
        mutableListOf<TrailerYardRuleRule>(
            TrailerYardRuleSourceLengthRule(32),
            TrailerYardRuleRequiredSourceRule("trailer yard rule needs an entry"),
            TrailerYardRuleRequiredLabelRule("trailer yard rule needs this field"),
            TrailerYardRuleCodePrefixRule("OPS"),
            TrailerYardRuleMarginRangeRule(9.9, 1952.6),
            TrailerYardRuleEtaMinutesRangeRule(120, 255),
        ),
    )
}
