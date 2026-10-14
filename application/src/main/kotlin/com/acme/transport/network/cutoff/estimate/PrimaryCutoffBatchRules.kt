package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.text.normalize

/** A rule a primary cutoff batch must pass. Check returns its problems, or none. */
interface PrimaryCutoffBatchRule {
    fun check(item: PrimaryCutoffBatch): MutableList<String>
}

data class PrimaryCutoffBatchCodePrefixRule(var prefix: String) : PrimaryCutoffBatchRule {
    override fun check(item: PrimaryCutoffBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffBatchNameLengthRule(var maxLength: Int) : PrimaryCutoffBatchRule {
    override fun check(item: PrimaryCutoffBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffBatchAllowedNameRule(var allowed: MutableList<String>) : PrimaryCutoffBatchRule {
    override fun check(item: PrimaryCutoffBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffBatchEtaMinutesRangeRule(var min: Int, var max: Int) : PrimaryCutoffBatchRule {
    override fun check(item: PrimaryCutoffBatch): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffBatchCodeLengthRule(var maxLength: Int) : PrimaryCutoffBatchRule {
    override fun check(item: PrimaryCutoffBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffBatchRequiredCodeRule(var message: String) : PrimaryCutoffBatchRule {
    override fun check(item: PrimaryCutoffBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary cutoff batch. */
data class PrimaryCutoffBatchValidator(var rules: MutableList<PrimaryCutoffBatchRule>) {
    fun validate(item: PrimaryCutoffBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryCutoffBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCutoffBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCutoffBatchValidator(): PrimaryCutoffBatchValidator {
    return PrimaryCutoffBatchValidator(
        mutableListOf<PrimaryCutoffBatchRule>(
            PrimaryCutoffBatchCodePrefixRule("REF"),
            PrimaryCutoffBatchNameLengthRule(3600),
            PrimaryCutoffBatchAllowedNameRule(mutableListOf<String>("primary", "batched", "lenient")),
            PrimaryCutoffBatchEtaMinutesRangeRule(80, 86400),
            PrimaryCutoffBatchCodeLengthRule(120),
            PrimaryCutoffBatchRequiredCodeRule("primary cutoff batch needs an entry"),
        ),
    )
}
