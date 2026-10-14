package com.acme.transport.linehaul.capacity.preference

/** A rule a primary capacity window must pass. Check returns its problems, or none. */
interface PrimaryCapacityWindowRule {
    fun check(item: PrimaryCapacityWindow): MutableList<String>
}

data class PrimaryCapacityWindowCodePrefixRule(var prefix: String) : PrimaryCapacityWindowRule {
    override fun check(item: PrimaryCapacityWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityWindowQuantityRangeRule(var min: Int, var max: Int) : PrimaryCapacityWindowRule {
    override fun check(item: PrimaryCapacityWindow): MutableList<String> {
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

data class PrimaryCapacityWindowRequiredCodeRule(var message: String) : PrimaryCapacityWindowRule {
    override fun check(item: PrimaryCapacityWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityWindowRequiredTitleRule(var message: String) : PrimaryCapacityWindowRule {
    override fun check(item: PrimaryCapacityWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacityWindowTitleLengthRule(var maxLength: Int) : PrimaryCapacityWindowRule {
    override fun check(item: PrimaryCapacityWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary capacity window. */
data class PrimaryCapacityWindowValidator(var rules: MutableList<PrimaryCapacityWindowRule>) {
    fun validate(item: PrimaryCapacityWindow): MutableList<String> {
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

    fun requireValid(item: PrimaryCapacityWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCapacityWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCapacityWindowValidator(): PrimaryCapacityWindowValidator {
    return PrimaryCapacityWindowValidator(
        mutableListOf<PrimaryCapacityWindowRule>(
            PrimaryCapacityWindowCodePrefixRule("ACM"),
            PrimaryCapacityWindowQuantityRangeRule(64, 86400),
            PrimaryCapacityWindowRequiredCodeRule("primary capacity window needs this field"),
            PrimaryCapacityWindowRequiredTitleRule("primary capacity window needs an entry"),
            PrimaryCapacityWindowTitleLengthRule(120),
        ),
    )
}
