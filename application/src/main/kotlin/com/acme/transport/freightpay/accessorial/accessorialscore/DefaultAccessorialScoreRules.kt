package com.acme.transport.freightpay.accessorial.accessorialscore

/** A rule a default accessorial score must pass. Check returns its problems, or none. */
interface DefaultAccessorialScoreRule {
    fun check(item: DefaultAccessorialScore): MutableList<String>
}

data class DefaultAccessorialScoreCodePrefixRule(var prefix: String) : DefaultAccessorialScoreRule {
    override fun check(item: DefaultAccessorialScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialScoreLabelLengthRule(var maxLength: Int) : DefaultAccessorialScoreRule {
    override fun check(item: DefaultAccessorialScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialScoreRequiredLabelRule(var message: String) : DefaultAccessorialScoreRule {
    override fun check(item: DefaultAccessorialScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialScoreChannelLengthRule(var maxLength: Int) : DefaultAccessorialScoreRule {
    override fun check(item: DefaultAccessorialScore): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default accessorial score. */
data class DefaultAccessorialScoreValidator(var rules: MutableList<DefaultAccessorialScoreRule>) {
    fun validate(item: DefaultAccessorialScore): MutableList<String> {
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

    fun requireValid(item: DefaultAccessorialScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAccessorialScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAccessorialScoreValidator(): DefaultAccessorialScoreValidator {
    return DefaultAccessorialScoreValidator(
        mutableListOf<DefaultAccessorialScoreRule>(
            DefaultAccessorialScoreCodePrefixRule("Q"),
            DefaultAccessorialScoreLabelLengthRule(86400),
            DefaultAccessorialScoreRequiredLabelRule("default accessorial score needs a value"),
            DefaultAccessorialScoreChannelLengthRule(86400),
        ),
    )
}
