package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.text.normalize

/** A rule a global utilization exception must pass. Check returns its problems, or none. */
interface GlobalUtilizationExceptionRule {
    fun check(item: GlobalUtilizationException): MutableList<String>
}

data class GlobalUtilizationExceptionNameLengthRule(var maxLength: Int) : GlobalUtilizationExceptionRule {
    override fun check(item: GlobalUtilizationException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationExceptionFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : GlobalUtilizationExceptionRule {
    override fun check(item: GlobalUtilizationException): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationExceptionCodeLengthRule(var maxLength: Int) : GlobalUtilizationExceptionRule {
    override fun check(item: GlobalUtilizationException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationExceptionRequiredChannelRule(var message: String) : GlobalUtilizationExceptionRule {
    override fun check(item: GlobalUtilizationException): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationExceptionAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : GlobalUtilizationExceptionRule {
    override fun check(item: GlobalUtilizationException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationExceptionChannelLengthRule(var maxLength: Int) : GlobalUtilizationExceptionRule {
    override fun check(item: GlobalUtilizationException): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global utilization exception. */
data class GlobalUtilizationExceptionValidator(var rules: MutableList<GlobalUtilizationExceptionRule>) {
    fun validate(item: GlobalUtilizationException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalUtilizationException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalUtilizationException>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalUtilizationExceptionValidator(): GlobalUtilizationExceptionValidator {
    return GlobalUtilizationExceptionValidator(
        mutableListOf<GlobalUtilizationExceptionRule>(
            GlobalUtilizationExceptionNameLengthRule(16),
            GlobalUtilizationExceptionFuelLitersRangeRule(7.7, 1224.3),
            GlobalUtilizationExceptionCodeLengthRule(32),
            GlobalUtilizationExceptionRequiredChannelRule("global utilization exception needs a value"),
            GlobalUtilizationExceptionAllowedContactEmailRule(mutableListOf<String>()),
            GlobalUtilizationExceptionChannelLengthRule(3600),
        ),
    )
}
