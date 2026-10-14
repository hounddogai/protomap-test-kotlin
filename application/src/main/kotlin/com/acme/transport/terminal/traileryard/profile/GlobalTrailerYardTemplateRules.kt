package com.acme.transport.terminal.traileryard.profile

/** A rule a global trailer yard template must pass. Check returns its problems, or none. */
interface GlobalTrailerYardTemplateRule {
    fun check(item: GlobalTrailerYardTemplate): MutableList<String>
}

data class GlobalTrailerYardTemplateRequiredContactEmailRule(
    var message: String,
) : GlobalTrailerYardTemplateRule {
    override fun check(item: GlobalTrailerYardTemplate): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardTemplateCodeLengthRule(var maxLength: Int) : GlobalTrailerYardTemplateRule {
    override fun check(item: GlobalTrailerYardTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardTemplateCodePrefixRule(var prefix: String) : GlobalTrailerYardTemplateRule {
    override fun check(item: GlobalTrailerYardTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardTemplateEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalTrailerYardTemplateRule {
    override fun check(item: GlobalTrailerYardTemplate): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class GlobalTrailerYardTemplateRetriesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalTrailerYardTemplateRule {
    override fun check(item: GlobalTrailerYardTemplate): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class GlobalTrailerYardTemplateTitleLengthRule(var maxLength: Int) : GlobalTrailerYardTemplateRule {
    override fun check(item: GlobalTrailerYardTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global trailer yard template. */
data class GlobalTrailerYardTemplateValidator(var rules: MutableList<GlobalTrailerYardTemplateRule>) {
    fun validate(item: GlobalTrailerYardTemplate): MutableList<String> {
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

    fun requireValid(item: GlobalTrailerYardTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerYardTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerYardTemplateValidator(): GlobalTrailerYardTemplateValidator {
    return GlobalTrailerYardTemplateValidator(
        mutableListOf<GlobalTrailerYardTemplateRule>(
            GlobalTrailerYardTemplateRequiredContactEmailRule("global trailer yard template needs a value"),
            GlobalTrailerYardTemplateCodeLengthRule(64),
            GlobalTrailerYardTemplateCodePrefixRule("ACM"),
            GlobalTrailerYardTemplateEtaMinutesRangeRule(120, 3600),
            GlobalTrailerYardTemplateRetriesRangeRule(120, 120),
            GlobalTrailerYardTemplateTitleLengthRule(16),
        ),
    )
}
