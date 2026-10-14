package com.acme.transport.freightpay.dispute.window

/** A rule a dispute template must pass. Check returns its problems, or none. */
interface DisputeTemplateRule {
    fun check(item: DisputeTemplate): MutableList<String>
}

data class DisputeTemplateEtaMinutesRangeRule(var min: Int, var max: Int) : DisputeTemplateRule {
    override fun check(item: DisputeTemplate): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeTemplateRequiredChannelRule(var message: String) : DisputeTemplateRule {
    override fun check(item: DisputeTemplate): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeTemplateRatioRangeRule(var min: Double, var max: Double) : DisputeTemplateRule {
    override fun check(item: DisputeTemplate): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class DisputeTemplateNameLengthRule(var maxLength: Int) : DisputeTemplateRule {
    override fun check(item: DisputeTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeTemplateDiscountRangeRule(var min: Double, var max: Double) : DisputeTemplateRule {
    override fun check(item: DisputeTemplate): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute template. */
data class DisputeTemplateValidator(var rules: MutableList<DisputeTemplateRule>) {
    fun validate(item: DisputeTemplate): MutableList<String> {
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

    fun requireValid(item: DisputeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeTemplateValidator(): DisputeTemplateValidator {
    return DisputeTemplateValidator(
        mutableListOf<DisputeTemplateRule>(
            DisputeTemplateEtaMinutesRangeRule(32, 64),
            DisputeTemplateRequiredChannelRule("dispute template needs an entry"),
            DisputeTemplateRatioRangeRule(5.1, 3323.7),
            DisputeTemplateNameLengthRule(1000),
            DisputeTemplateDiscountRangeRule(9.9, 888.7),
        ),
    )
}
