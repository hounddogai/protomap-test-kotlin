package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.text.normalize

/** A rule a tariff target must pass. Check returns its problems, or none. */
interface TariffTargetRule {
    fun check(item: TariffTarget): MutableList<String>
}

data class TariffTargetRequiredTitleRule(var message: String) : TariffTargetRule {
    override fun check(item: TariffTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffTargetRequiredCodeRule(var message: String) : TariffTargetRule {
    override fun check(item: TariffTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffTargetCodePrefixRule(var prefix: String) : TariffTargetRule {
    override fun check(item: TariffTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffTargetAllowedTitleRule(var allowed: MutableList<String>) : TariffTargetRule {
    override fun check(item: TariffTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class TariffTargetRouteCodeLengthRule(var maxLength: Int) : TariffTargetRule {
    override fun check(item: TariffTarget): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff target. */
data class TariffTargetValidator(var rules: MutableList<TariffTargetRule>) {
    fun validate(item: TariffTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffTargetValidator(): TariffTargetValidator {
    return TariffTargetValidator(
        mutableListOf<TariffTargetRule>(
            TariffTargetRequiredTitleRule("tariff target needs a value"),
            TariffTargetRequiredCodeRule("tariff target needs this field"),
            TariffTargetCodePrefixRule("Q"),
            TariffTargetAllowedTitleRule(mutableListOf<String>("weighted", "manual")),
            TariffTargetRouteCodeLengthRule(1000),
        ),
    )
}
