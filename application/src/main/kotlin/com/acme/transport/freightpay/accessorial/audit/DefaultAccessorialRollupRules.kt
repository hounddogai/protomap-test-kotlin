package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.kit.text.normalize

/** A rule a default accessorial rollup must pass. Check returns its problems, or none. */
interface DefaultAccessorialRollupRule {
    fun check(item: DefaultAccessorialRollup): MutableList<String>
}

data class DefaultAccessorialRollupChecksumLengthRule(var maxLength: Int) : DefaultAccessorialRollupRule {
    override fun check(item: DefaultAccessorialRollup): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialRollupAllowedChannelRule(
    var allowed: MutableList<String>,
) : DefaultAccessorialRollupRule {
    override fun check(item: DefaultAccessorialRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialRollupRequiredChecksumRule(var message: String) : DefaultAccessorialRollupRule {
    override fun check(item: DefaultAccessorialRollup): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialRollupCodePrefixRule(var prefix: String) : DefaultAccessorialRollupRule {
    override fun check(item: DefaultAccessorialRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialRollupRequiredNameRule(var message: String) : DefaultAccessorialRollupRule {
    override fun check(item: DefaultAccessorialRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialRollupChannelLengthRule(var maxLength: Int) : DefaultAccessorialRollupRule {
    override fun check(item: DefaultAccessorialRollup): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default accessorial rollup. */
data class DefaultAccessorialRollupValidator(var rules: MutableList<DefaultAccessorialRollupRule>) {
    fun validate(item: DefaultAccessorialRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAccessorialRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAccessorialRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAccessorialRollupValidator(): DefaultAccessorialRollupValidator {
    return DefaultAccessorialRollupValidator(
        mutableListOf<DefaultAccessorialRollupRule>(
            DefaultAccessorialRollupChecksumLengthRule(3600),
            DefaultAccessorialRollupAllowedChannelRule(mutableListOf<String>("adaptive", "rolling")),
            DefaultAccessorialRollupRequiredChecksumRule("default accessorial rollup needs a value"),
            DefaultAccessorialRollupCodePrefixRule("REF"),
            DefaultAccessorialRollupRequiredNameRule("default accessorial rollup needs an entry"),
            DefaultAccessorialRollupChannelLengthRule(32),
        ),
    )
}
