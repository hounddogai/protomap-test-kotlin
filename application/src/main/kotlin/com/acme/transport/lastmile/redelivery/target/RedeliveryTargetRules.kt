package com.acme.transport.lastmile.redelivery.target

/** A rule a redelivery target must pass. Check returns its problems, or none. */
interface RedeliveryTargetRule {
    fun check(item: RedeliveryTarget): MutableList<String>
}

data class RedeliveryTargetRequiredCodeRule(var message: String) : RedeliveryTargetRule {
    override fun check(item: RedeliveryTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryTargetNameLengthRule(var maxLength: Int) : RedeliveryTargetRule {
    override fun check(item: RedeliveryTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryTargetRequiredCurrencyRule(var message: String) : RedeliveryTargetRule {
    override fun check(item: RedeliveryTarget): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryTargetCurrencyLengthRule(var maxLength: Int) : RedeliveryTargetRule {
    override fun check(item: RedeliveryTarget): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryTargetCodeLengthRule(var maxLength: Int) : RedeliveryTargetRule {
    override fun check(item: RedeliveryTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery target. */
data class RedeliveryTargetValidator(var rules: MutableList<RedeliveryTargetRule>) {
    fun validate(item: RedeliveryTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryTargetValidator(): RedeliveryTargetValidator {
    return RedeliveryTargetValidator(
        mutableListOf<RedeliveryTargetRule>(
            RedeliveryTargetRequiredCodeRule("redelivery target needs this field"),
            RedeliveryTargetNameLengthRule(255),
            RedeliveryTargetRequiredCurrencyRule("redelivery target needs this field"),
            RedeliveryTargetCurrencyLengthRule(32),
            RedeliveryTargetCodeLengthRule(1000),
        ),
    )
}
