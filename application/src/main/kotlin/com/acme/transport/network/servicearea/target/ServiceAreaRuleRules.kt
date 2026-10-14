package com.acme.transport.network.servicearea.target

/** A rule a service area rule must pass. Check returns its problems, or none. */
interface ServiceAreaRuleRule {
    fun check(item: ServiceAreaRule): MutableList<String>
}

data class ServiceAreaRuleShiftCodeLengthRule(var maxLength: Int) : ServiceAreaRuleRule {
    override fun check(item: ServiceAreaRule): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRuleCodeLengthRule(var maxLength: Int) : ServiceAreaRuleRule {
    override fun check(item: ServiceAreaRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRuleRequiredCodeRule(var message: String) : ServiceAreaRuleRule {
    override fun check(item: ServiceAreaRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRuleGraceDaysRangeRule(var min: Int, var max: Int) : ServiceAreaRuleRule {
    override fun check(item: ServiceAreaRule): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area rule. */
data class ServiceAreaRuleValidator(var rules: MutableList<ServiceAreaRuleRule>) {
    fun validate(item: ServiceAreaRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaRule>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaRuleValidator(): ServiceAreaRuleValidator {
    return ServiceAreaRuleValidator(
        mutableListOf<ServiceAreaRuleRule>(
            ServiceAreaRuleShiftCodeLengthRule(16),
            ServiceAreaRuleCodeLengthRule(255),
            ServiceAreaRuleRequiredCodeRule("service area rule needs this field"),
            ServiceAreaRuleGraceDaysRangeRule(32, 3600),
        ),
    )
}
