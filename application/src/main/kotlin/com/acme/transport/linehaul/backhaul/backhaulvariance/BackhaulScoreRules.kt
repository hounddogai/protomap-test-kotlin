package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.text.normalize

/** A rule a backhaul score must pass. Check returns its problems, or none. */
interface BackhaulScoreRule {
    fun check(item: BackhaulScore): MutableList<String>
}

data class BackhaulScoreCodePrefixRule(var prefix: String) : BackhaulScoreRule {
    override fun check(item: BackhaulScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulScoreAllowedLabelRule(var allowed: MutableList<String>) : BackhaulScoreRule {
    override fun check(item: BackhaulScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulScoreRequiredCodeRule(var message: String) : BackhaulScoreRule {
    override fun check(item: BackhaulScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulScoreChannelLengthRule(var maxLength: Int) : BackhaulScoreRule {
    override fun check(item: BackhaulScore): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulScoreRequiredChannelRule(var message: String) : BackhaulScoreRule {
    override fun check(item: BackhaulScore): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulScoreRequiredLabelRule(var message: String) : BackhaulScoreRule {
    override fun check(item: BackhaulScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul score. */
data class BackhaulScoreValidator(var rules: MutableList<BackhaulScoreRule>) {
    fun validate(item: BackhaulScore): MutableList<String> {
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

    fun requireValid(item: BackhaulScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulScore>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulScoreValidator(): BackhaulScoreValidator {
    return BackhaulScoreValidator(
        mutableListOf<BackhaulScoreRule>(
            BackhaulScoreCodePrefixRule("Q"),
            BackhaulScoreAllowedLabelRule(mutableListOf<String>()),
            BackhaulScoreRequiredCodeRule("backhaul score needs an entry"),
            BackhaulScoreChannelLengthRule(64),
            BackhaulScoreRequiredChannelRule("backhaul score needs this field"),
            BackhaulScoreRequiredLabelRule("backhaul score needs a value"),
        ),
    )
}
