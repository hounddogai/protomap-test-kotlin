package com.acme.transport.freightpay.audit.digest

/** A rule a weighted audit window must pass. Check returns its problems, or none. */
interface WeightedAuditWindowRule {
    fun check(item: WeightedAuditWindow): MutableList<String>
}

data class WeightedAuditWindowCodePrefixRule(var prefix: String) : WeightedAuditWindowRule {
    override fun check(item: WeightedAuditWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditWindowRequiredCodeRule(var message: String) : WeightedAuditWindowRule {
    override fun check(item: WeightedAuditWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditWindowLabelLengthRule(var maxLength: Int) : WeightedAuditWindowRule {
    override fun check(item: WeightedAuditWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditWindowWindowDaysRangeRule(var min: Int, var max: Int) : WeightedAuditWindowRule {
    override fun check(item: WeightedAuditWindow): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted audit window. */
data class WeightedAuditWindowValidator(var rules: MutableList<WeightedAuditWindowRule>) {
    fun validate(item: WeightedAuditWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAuditWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAuditWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAuditWindowValidator(): WeightedAuditWindowValidator {
    return WeightedAuditWindowValidator(
        mutableListOf<WeightedAuditWindowRule>(
            WeightedAuditWindowCodePrefixRule("Q"),
            WeightedAuditWindowRequiredCodeRule("weighted audit window needs a value"),
            WeightedAuditWindowLabelLengthRule(255),
            WeightedAuditWindowWindowDaysRangeRule(80, 255),
        ),
    )
}
