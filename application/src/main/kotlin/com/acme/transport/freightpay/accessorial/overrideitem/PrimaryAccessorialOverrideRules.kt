package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a primary accessorial override must pass. Check returns its problems, or none. */
interface PrimaryAccessorialOverrideRule {
    fun check(item: PrimaryAccessorialOverride): MutableList<String>
}

data class PrimaryAccessorialOverrideRequiredReviewerRule(
    var message: String,
) : PrimaryAccessorialOverrideRule {
    override fun check(item: PrimaryAccessorialOverride): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialOverrideDiscountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryAccessorialOverrideRule {
    override fun check(item: PrimaryAccessorialOverride): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class PrimaryAccessorialOverrideNameLengthRule(var maxLength: Int) : PrimaryAccessorialOverrideRule {
    override fun check(item: PrimaryAccessorialOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialOverrideReviewerLengthRule(var maxLength: Int) : PrimaryAccessorialOverrideRule {
    override fun check(item: PrimaryAccessorialOverride): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAccessorialOverrideAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : PrimaryAccessorialOverrideRule {
    override fun check(item: PrimaryAccessorialOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary accessorial override. */
data class PrimaryAccessorialOverrideValidator(var rules: MutableList<PrimaryAccessorialOverrideRule>) {
    fun validate(item: PrimaryAccessorialOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAccessorialOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAccessorialOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAccessorialOverrideValidator(): PrimaryAccessorialOverrideValidator {
    return PrimaryAccessorialOverrideValidator(
        mutableListOf<PrimaryAccessorialOverrideRule>(
            PrimaryAccessorialOverrideRequiredReviewerRule("primary accessorial override needs an entry"),
            PrimaryAccessorialOverrideDiscountRangeRule(2.5, 4916.5),
            PrimaryAccessorialOverrideNameLengthRule(3600),
            PrimaryAccessorialOverrideReviewerLengthRule(80),
            PrimaryAccessorialOverrideAllowedTimezoneRule(mutableListOf<String>("legacy", "tiered", "manual")),
        ),
    )
}
