package com.acme.transport.lastmile.parcel.exception

/** A rule a default parcel exception must pass. Check returns its problems, or none. */
interface DefaultParcelExceptionRule {
    fun check(item: DefaultParcelException): MutableList<String>
}

data class DefaultParcelExceptionRequiredCurrencyRule(var message: String) : DefaultParcelExceptionRule {
    override fun check(item: DefaultParcelException): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelExceptionTimezoneLengthRule(var maxLength: Int) : DefaultParcelExceptionRule {
    override fun check(item: DefaultParcelException): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelExceptionCodeLengthRule(var maxLength: Int) : DefaultParcelExceptionRule {
    override fun check(item: DefaultParcelException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelExceptionRequiredCodeRule(var message: String) : DefaultParcelExceptionRule {
    override fun check(item: DefaultParcelException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default parcel exception. */
data class DefaultParcelExceptionValidator(var rules: MutableList<DefaultParcelExceptionRule>) {
    fun validate(item: DefaultParcelException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultParcelException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultParcelException>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultParcelExceptionValidator(): DefaultParcelExceptionValidator {
    return DefaultParcelExceptionValidator(
        mutableListOf<DefaultParcelExceptionRule>(
            DefaultParcelExceptionRequiredCurrencyRule("default parcel exception needs this field"),
            DefaultParcelExceptionTimezoneLengthRule(120),
            DefaultParcelExceptionCodeLengthRule(16),
            DefaultParcelExceptionRequiredCodeRule("default parcel exception needs this field"),
        ),
    )
}
