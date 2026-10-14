package com.acme.transport.freightpay.accessorial.draft

/** A rule a accessorial template must pass. Check returns its problems, or none. */
interface AccessorialTemplateRule {
    fun check(item: AccessorialTemplate): MutableList<String>
}

data class AccessorialTemplateChannelLengthRule(var maxLength: Int) : AccessorialTemplateRule {
    override fun check(item: AccessorialTemplate): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialTemplateRequiredChannelRule(var message: String) : AccessorialTemplateRule {
    override fun check(item: AccessorialTemplate): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialTemplateOdometerRangeRule(var min: Int, var max: Int) : AccessorialTemplateRule {
    override fun check(item: AccessorialTemplate): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class AccessorialTemplateRequiredNameRule(var message: String) : AccessorialTemplateRule {
    override fun check(item: AccessorialTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialTemplateCodeLengthRule(var maxLength: Int) : AccessorialTemplateRule {
    override fun check(item: AccessorialTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial template. */
data class AccessorialTemplateValidator(var rules: MutableList<AccessorialTemplateRule>) {
    fun validate(item: AccessorialTemplate): MutableList<String> {
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

    fun requireValid(item: AccessorialTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialTemplateValidator(): AccessorialTemplateValidator {
    return AccessorialTemplateValidator(
        mutableListOf<AccessorialTemplateRule>(
            AccessorialTemplateChannelLengthRule(80),
            AccessorialTemplateRequiredChannelRule("accessorial template needs a value"),
            AccessorialTemplateOdometerRangeRule(64, 80),
            AccessorialTemplateRequiredNameRule("accessorial template needs this field"),
            AccessorialTemplateCodeLengthRule(1000),
        ),
    )
}
