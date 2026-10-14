package com.acme.transport.terminal.crossdock.profile

/** A rule a cross dock window must pass. Check returns its problems, or none. */
interface CrossDockWindowRule {
    fun check(item: CrossDockWindow): MutableList<String>
}

data class CrossDockWindowLabelLengthRule(var maxLength: Int) : CrossDockWindowRule {
    override fun check(item: CrossDockWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockWindowRequiredLabelRule(var message: String) : CrossDockWindowRule {
    override fun check(item: CrossDockWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockWindowMaximumRangeRule(var min: Double, var max: Double) : CrossDockWindowRule {
    override fun check(item: CrossDockWindow): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockWindowRequiredLaneRule(var message: String) : CrossDockWindowRule {
    override fun check(item: CrossDockWindow): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockWindowCurrencyLengthRule(var maxLength: Int) : CrossDockWindowRule {
    override fun check(item: CrossDockWindow): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockWindowQuantityRangeRule(var min: Int, var max: Int) : CrossDockWindowRule {
    override fun check(item: CrossDockWindow): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a cross dock window. */
data class CrossDockWindowValidator(var rules: MutableList<CrossDockWindowRule>) {
    fun validate(item: CrossDockWindow): MutableList<String> {
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

    fun requireValid(item: CrossDockWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockWindowValidator(): CrossDockWindowValidator {
    return CrossDockWindowValidator(
        mutableListOf<CrossDockWindowRule>(
            CrossDockWindowLabelLengthRule(16),
            CrossDockWindowRequiredLabelRule("cross dock window needs this field"),
            CrossDockWindowMaximumRangeRule(5.6, 1314.0),
            CrossDockWindowRequiredLaneRule("cross dock window needs this field"),
            CrossDockWindowCurrencyLengthRule(64),
            CrossDockWindowQuantityRangeRule(32, 64),
        ),
    )
}
