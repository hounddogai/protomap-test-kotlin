package com.acme.transport.lastmile.redelivery.rule

/** A rule a manual redelivery exception must pass. Check returns its problems, or none. */
interface ManualRedeliveryExceptionRule {
    fun check(item: ManualRedeliveryException): MutableList<String>
}

data class ManualRedeliveryExceptionRateRangeRule(
    var min: Double,
    var max: Double,
) : ManualRedeliveryExceptionRule {
    override fun check(item: ManualRedeliveryException): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualRedeliveryExceptionLocaleLengthRule(var maxLength: Int) : ManualRedeliveryExceptionRule {
    override fun check(item: ManualRedeliveryException): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRedeliveryExceptionVehicleIdLengthRule(var maxLength: Int) : ManualRedeliveryExceptionRule {
    override fun check(item: ManualRedeliveryException): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual redelivery exception. */
data class ManualRedeliveryExceptionValidator(var rules: MutableList<ManualRedeliveryExceptionRule>) {
    fun validate(item: ManualRedeliveryException): MutableList<String> {
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

    fun requireValid(item: ManualRedeliveryException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRedeliveryException>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRedeliveryExceptionValidator(): ManualRedeliveryExceptionValidator {
    return ManualRedeliveryExceptionValidator(
        mutableListOf<ManualRedeliveryExceptionRule>(
            ManualRedeliveryExceptionRateRangeRule(9.7, 3301.3),
            ManualRedeliveryExceptionLocaleLengthRule(255),
            ManualRedeliveryExceptionVehicleIdLengthRule(255),
        ),
    )
}
