package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.text.normalize

/** A rule a default swap checkpoint must pass. Check returns its problems, or none. */
interface DefaultSwapCheckpointRule {
    fun check(item: DefaultSwapCheckpoint): MutableList<String>
}

data class DefaultSwapCheckpointAllowedCodeRule(var allowed: MutableList<String>) : DefaultSwapCheckpointRule {
    override fun check(item: DefaultSwapCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSwapCheckpointRequiredExternalRefRule(var message: String) : DefaultSwapCheckpointRule {
    override fun check(item: DefaultSwapCheckpoint): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultSwapCheckpointCodePrefixRule(var prefix: String) : DefaultSwapCheckpointRule {
    override fun check(item: DefaultSwapCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSwapCheckpointTitleLengthRule(var maxLength: Int) : DefaultSwapCheckpointRule {
    override fun check(item: DefaultSwapCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default swap checkpoint. */
data class DefaultSwapCheckpointValidator(var rules: MutableList<DefaultSwapCheckpointRule>) {
    fun validate(item: DefaultSwapCheckpoint): MutableList<String> {
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

    fun requireValid(item: DefaultSwapCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultSwapCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultSwapCheckpointValidator(): DefaultSwapCheckpointValidator {
    return DefaultSwapCheckpointValidator(
        mutableListOf<DefaultSwapCheckpointRule>(
            DefaultSwapCheckpointAllowedCodeRule(mutableListOf<String>("pending", "fallback")),
            DefaultSwapCheckpointRequiredExternalRefRule("default swap checkpoint needs this field"),
            DefaultSwapCheckpointCodePrefixRule("OPS"),
            DefaultSwapCheckpointTitleLengthRule(1000),
        ),
    )
}
