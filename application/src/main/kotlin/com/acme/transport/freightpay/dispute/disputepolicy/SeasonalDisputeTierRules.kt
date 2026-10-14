package com.acme.transport.freightpay.dispute.disputepolicy

/** A rule a seasonal dispute tier must pass. Check returns its problems, or none. */
interface SeasonalDisputeTierRule {
    fun check(item: SeasonalDisputeTier): MutableList<String>
}

data class SeasonalDisputeTierRequiredCodeRule(var message: String) : SeasonalDisputeTierRule {
    override fun check(item: SeasonalDisputeTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDisputeTierTitleLengthRule(var maxLength: Int) : SeasonalDisputeTierRule {
    override fun check(item: SeasonalDisputeTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDisputeTierCodeLengthRule(var maxLength: Int) : SeasonalDisputeTierRule {
    override fun check(item: SeasonalDisputeTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDisputeTierCodePrefixRule(var prefix: String) : SeasonalDisputeTierRule {
    override fun check(item: SeasonalDisputeTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dispute tier. */
data class SeasonalDisputeTierValidator(var rules: MutableList<SeasonalDisputeTierRule>) {
    fun validate(item: SeasonalDisputeTier): MutableList<String> {
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

    fun requireValid(item: SeasonalDisputeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDisputeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDisputeTierValidator(): SeasonalDisputeTierValidator {
    return SeasonalDisputeTierValidator(
        mutableListOf<SeasonalDisputeTierRule>(
            SeasonalDisputeTierRequiredCodeRule("seasonal dispute tier needs a value"),
            SeasonalDisputeTierTitleLengthRule(120),
            SeasonalDisputeTierCodeLengthRule(16),
            SeasonalDisputeTierCodePrefixRule("Q"),
        ),
    )
}
