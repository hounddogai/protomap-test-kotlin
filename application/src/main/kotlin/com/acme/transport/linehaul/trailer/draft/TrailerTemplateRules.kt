package com.acme.transport.linehaul.trailer.draft

/** A rule a trailer template must pass. Check returns its problems, or none. */
interface TrailerTemplateRule {
    fun check(item: TrailerTemplate): MutableList<String>
}

data class TrailerTemplateCodePrefixRule(var prefix: String) : TrailerTemplateRule {
    override fun check(item: TrailerTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerTemplateRequiredChannelRule(var message: String) : TrailerTemplateRule {
    override fun check(item: TrailerTemplate): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerTemplateRequiredTitleRule(var message: String) : TrailerTemplateRule {
    override fun check(item: TrailerTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerTemplateFloorValueRangeRule(var min: Double, var max: Double) : TrailerTemplateRule {
    override fun check(item: TrailerTemplate): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class TrailerTemplateCodeLengthRule(var maxLength: Int) : TrailerTemplateRule {
    override fun check(item: TrailerTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer template. */
data class TrailerTemplateValidator(var rules: MutableList<TrailerTemplateRule>) {
    fun validate(item: TrailerTemplate): MutableList<String> {
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

    fun requireValid(item: TrailerTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerTemplateValidator(): TrailerTemplateValidator {
    return TrailerTemplateValidator(
        mutableListOf<TrailerTemplateRule>(
            TrailerTemplateCodePrefixRule("OPS"),
            TrailerTemplateRequiredChannelRule("trailer template needs this field"),
            TrailerTemplateRequiredTitleRule("trailer template needs a value"),
            TrailerTemplateFloorValueRangeRule(0.5, 819.8),
            TrailerTemplateCodeLengthRule(32),
        ),
    )
}
