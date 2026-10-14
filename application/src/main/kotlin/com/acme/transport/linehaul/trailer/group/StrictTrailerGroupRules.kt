package com.acme.transport.linehaul.trailer.group

/** A rule a strict trailer group must pass. Check returns its problems, or none. */
interface StrictTrailerGroupRule {
    fun check(item: StrictTrailerGroup): MutableList<String>
}

data class StrictTrailerGroupQuantityRangeRule(var min: Int, var max: Int) : StrictTrailerGroupRule {
    override fun check(item: StrictTrailerGroup): MutableList<String> {
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

data class StrictTrailerGroupNameLengthRule(var maxLength: Int) : StrictTrailerGroupRule {
    override fun check(item: StrictTrailerGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerGroupCodeLengthRule(var maxLength: Int) : StrictTrailerGroupRule {
    override fun check(item: StrictTrailerGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerGroupDriverIdLengthRule(var maxLength: Int) : StrictTrailerGroupRule {
    override fun check(item: StrictTrailerGroup): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerGroupCodePrefixRule(var prefix: String) : StrictTrailerGroupRule {
    override fun check(item: StrictTrailerGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerGroupRequiredCodeRule(var message: String) : StrictTrailerGroupRule {
    override fun check(item: StrictTrailerGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict trailer group. */
data class StrictTrailerGroupValidator(var rules: MutableList<StrictTrailerGroupRule>) {
    fun validate(item: StrictTrailerGroup): MutableList<String> {
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

    fun requireValid(item: StrictTrailerGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTrailerGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTrailerGroupValidator(): StrictTrailerGroupValidator {
    return StrictTrailerGroupValidator(
        mutableListOf<StrictTrailerGroupRule>(
            StrictTrailerGroupQuantityRangeRule(80, 255),
            StrictTrailerGroupNameLengthRule(120),
            StrictTrailerGroupCodeLengthRule(255),
            StrictTrailerGroupDriverIdLengthRule(3600),
            StrictTrailerGroupCodePrefixRule("X"),
            StrictTrailerGroupRequiredCodeRule("strict trailer group needs a value"),
        ),
    )
}
