package com.acme.transport.network.servicearea.rollup

/** A rule a service area rollup must pass. Check returns its problems, or none. */
interface ServiceAreaRollupRule {
    fun check(item: ServiceAreaRollup): MutableList<String>
}

data class ServiceAreaRollupCodePrefixRule(var prefix: String) : ServiceAreaRollupRule {
    override fun check(item: ServiceAreaRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRollupRequiredCodeRule(var message: String) : ServiceAreaRollupRule {
    override fun check(item: ServiceAreaRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRollupGraceDaysRangeRule(var min: Int, var max: Int) : ServiceAreaRollupRule {
    override fun check(item: ServiceAreaRollup): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaRollupTitleLengthRule(var maxLength: Int) : ServiceAreaRollupRule {
    override fun check(item: ServiceAreaRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRollupLabelLengthRule(var maxLength: Int) : ServiceAreaRollupRule {
    override fun check(item: ServiceAreaRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area rollup. */
data class ServiceAreaRollupValidator(var rules: MutableList<ServiceAreaRollupRule>) {
    fun validate(item: ServiceAreaRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaRollupValidator(): ServiceAreaRollupValidator {
    return ServiceAreaRollupValidator(
        mutableListOf<ServiceAreaRollupRule>(
            ServiceAreaRollupCodePrefixRule("Q"),
            ServiceAreaRollupRequiredCodeRule("service area rollup needs an entry"),
            ServiceAreaRollupGraceDaysRangeRule(32, 1000),
            ServiceAreaRollupTitleLengthRule(1000),
            ServiceAreaRollupLabelLengthRule(16),
        ),
    )
}
