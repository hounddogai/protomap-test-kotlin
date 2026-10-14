package com.acme.transport.linehaul.tender.group

/** A rule a lenient tender group must pass. Check returns its problems, or none. */
interface LenientTenderGroupRule {
    fun check(item: LenientTenderGroup): MutableList<String>
}

data class LenientTenderGroupCountryLengthRule(var maxLength: Int) : LenientTenderGroupRule {
    override fun check(item: LenientTenderGroup): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTenderGroupCodePrefixRule(var prefix: String) : LenientTenderGroupRule {
    override fun check(item: LenientTenderGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientTenderGroupRequiredCountryRule(var message: String) : LenientTenderGroupRule {
    override fun check(item: LenientTenderGroup): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTenderGroupMaximumRangeRule(var min: Double, var max: Double) : LenientTenderGroupRule {
    override fun check(item: LenientTenderGroup): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient tender group. */
data class LenientTenderGroupValidator(var rules: MutableList<LenientTenderGroupRule>) {
    fun validate(item: LenientTenderGroup): MutableList<String> {
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

    fun requireValid(item: LenientTenderGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTenderGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTenderGroupValidator(): LenientTenderGroupValidator {
    return LenientTenderGroupValidator(
        mutableListOf<LenientTenderGroupRule>(
            LenientTenderGroupCountryLengthRule(255),
            LenientTenderGroupCodePrefixRule("ACM"),
            LenientTenderGroupRequiredCountryRule("lenient tender group needs an entry"),
            LenientTenderGroupMaximumRangeRule(0.5, 4120.6),
        ),
    )
}
