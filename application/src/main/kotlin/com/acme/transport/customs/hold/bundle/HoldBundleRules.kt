package com.acme.transport.customs.hold.bundle

/** A rule a hold bundle must pass. Check returns its problems, or none. */
interface HoldBundleRule {
    fun check(item: HoldBundle): MutableList<String>
}

data class HoldBundleEndsAtRangeRule(var min: Int, var max: Int) : HoldBundleRule {
    override fun check(item: HoldBundle): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldBundleContactEmailLengthRule(var maxLength: Int) : HoldBundleRule {
    override fun check(item: HoldBundle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldBundleBudgetRangeRule(var min: Double, var max: Double) : HoldBundleRule {
    override fun check(item: HoldBundle): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldBundleRequiredCodeRule(var message: String) : HoldBundleRule {
    override fun check(item: HoldBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldBundleCodeLengthRule(var maxLength: Int) : HoldBundleRule {
    override fun check(item: HoldBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldBundleCodePrefixRule(var prefix: String) : HoldBundleRule {
    override fun check(item: HoldBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold bundle. */
data class HoldBundleValidator(var rules: MutableList<HoldBundleRule>) {
    fun validate(item: HoldBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldBundleValidator(): HoldBundleValidator {
    return HoldBundleValidator(
        mutableListOf<HoldBundleRule>(
            HoldBundleEndsAtRangeRule(120, 1000),
            HoldBundleContactEmailLengthRule(64),
            HoldBundleBudgetRangeRule(0.8, 2707.4),
            HoldBundleRequiredCodeRule("hold bundle needs an entry"),
            HoldBundleCodeLengthRule(86400),
            HoldBundleCodePrefixRule("ACM"),
        ),
    )
}
