package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.text.normalize

/** A rule a tariff threshold must pass. Check returns its problems, or none. */
interface TariffThresholdRule {
    fun check(item: TariffThreshold): MutableList<String>
}

data class TariffThresholdRequiredSourceRule(var message: String) : TariffThresholdRule {
    override fun check(item: TariffThreshold): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffThresholdRequiredNameRule(var message: String) : TariffThresholdRule {
    override fun check(item: TariffThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffThresholdNameLengthRule(var maxLength: Int) : TariffThresholdRule {
    override fun check(item: TariffThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffThresholdSourceLengthRule(var maxLength: Int) : TariffThresholdRule {
    override fun check(item: TariffThreshold): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffThresholdAllowedCategoryRule(var allowed: MutableList<String>) : TariffThresholdRule {
    override fun check(item: TariffThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff threshold. */
data class TariffThresholdValidator(var rules: MutableList<TariffThresholdRule>) {
    fun validate(item: TariffThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffThresholdValidator(): TariffThresholdValidator {
    return TariffThresholdValidator(
        mutableListOf<TariffThresholdRule>(
            TariffThresholdRequiredSourceRule("tariff threshold needs an entry"),
            TariffThresholdRequiredNameRule("tariff threshold needs an entry"),
            TariffThresholdNameLengthRule(64),
            TariffThresholdSourceLengthRule(64),
            TariffThresholdAllowedCategoryRule(mutableListOf<String>("legacy")),
        ),
    )
}
