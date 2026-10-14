package com.acme.transport.linehaul.tender.plan

/** A rule a tender variance must pass. Check returns its problems, or none. */
interface TenderVarianceRule {
    fun check(item: TenderVariance): MutableList<String>
}

data class TenderVarianceRequiredPhoneRule(var message: String) : TenderVarianceRule {
    override fun check(item: TenderVariance): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderVarianceRevisionRangeRule(var min: Int, var max: Int) : TenderVarianceRule {
    override fun check(item: TenderVariance): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderVarianceCodePrefixRule(var prefix: String) : TenderVarianceRule {
    override fun check(item: TenderVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderVariancePhoneLengthRule(var maxLength: Int) : TenderVarianceRule {
    override fun check(item: TenderVariance): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderVarianceCodeLengthRule(var maxLength: Int) : TenderVarianceRule {
    override fun check(item: TenderVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderVarianceQuantityRangeRule(var min: Int, var max: Int) : TenderVarianceRule {
    override fun check(item: TenderVariance): MutableList<String> {
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

/** Runs every rule of a tender variance. */
data class TenderVarianceValidator(var rules: MutableList<TenderVarianceRule>) {
    fun validate(item: TenderVariance): MutableList<String> {
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

    fun requireValid(item: TenderVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderVarianceValidator(): TenderVarianceValidator {
    return TenderVarianceValidator(
        mutableListOf<TenderVarianceRule>(
            TenderVarianceRequiredPhoneRule("tender variance needs a value"),
            TenderVarianceRevisionRangeRule(80, 120),
            TenderVarianceCodePrefixRule("ACM"),
            TenderVariancePhoneLengthRule(86400),
            TenderVarianceCodeLengthRule(32),
            TenderVarianceQuantityRangeRule(64, 80),
        ),
    )
}
