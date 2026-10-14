package com.acme.transport.terminal.traileryard.yardsummary

/** A rule a tiered trailer yard exception must pass. Check returns its problems, or none. */
interface TieredTrailerYardExceptionRule {
    fun check(item: TieredTrailerYardException): MutableList<String>
}

data class TieredTrailerYardExceptionRequiredPhoneRule(var message: String) : TieredTrailerYardExceptionRule {
    override fun check(item: TieredTrailerYardException): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardExceptionQuantityRangeRule(
    var min: Int,
    var max: Int,
) : TieredTrailerYardExceptionRule {
    override fun check(item: TieredTrailerYardException): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardExceptionCodeLengthRule(var maxLength: Int) : TieredTrailerYardExceptionRule {
    override fun check(item: TieredTrailerYardException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardExceptionRequiredOwnerIdRule(var message: String) : TieredTrailerYardExceptionRule {
    override fun check(item: TieredTrailerYardException): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardExceptionRequiredCodeRule(var message: String) : TieredTrailerYardExceptionRule {
    override fun check(item: TieredTrailerYardException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardExceptionCodePrefixRule(var prefix: String) : TieredTrailerYardExceptionRule {
    override fun check(item: TieredTrailerYardException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered trailer yard exception. */
data class TieredTrailerYardExceptionValidator(var rules: MutableList<TieredTrailerYardExceptionRule>) {
    fun validate(item: TieredTrailerYardException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredTrailerYardException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTrailerYardException>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTrailerYardExceptionValidator(): TieredTrailerYardExceptionValidator {
    return TieredTrailerYardExceptionValidator(
        mutableListOf<TieredTrailerYardExceptionRule>(
            TieredTrailerYardExceptionRequiredPhoneRule("tiered trailer yard exception needs a value"),
            TieredTrailerYardExceptionQuantityRangeRule(16, 64),
            TieredTrailerYardExceptionCodeLengthRule(1000),
            TieredTrailerYardExceptionRequiredOwnerIdRule("tiered trailer yard exception needs this field"),
            TieredTrailerYardExceptionRequiredCodeRule("tiered trailer yard exception needs a value"),
            TieredTrailerYardExceptionCodePrefixRule("ACM"),
        ),
    )
}
