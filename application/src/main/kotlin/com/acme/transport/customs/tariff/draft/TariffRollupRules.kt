package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.text.normalize

/** A rule a tariff rollup must pass. Check returns its problems, or none. */
interface TariffRollupRule {
    fun check(item: TariffRollup): MutableList<String>
}

data class TariffRollupCodePrefixRule(var prefix: String) : TariffRollupRule {
    override fun check(item: TariffRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffRollupExternalRefLengthRule(var maxLength: Int) : TariffRollupRule {
    override fun check(item: TariffRollup): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffRollupAllowedDockRule(var allowed: MutableList<String>) : TariffRollupRule {
    override fun check(item: TariffRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TariffRollupRequiredExternalRefRule(var message: String) : TariffRollupRule {
    override fun check(item: TariffRollup): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff rollup. */
data class TariffRollupValidator(var rules: MutableList<TariffRollupRule>) {
    fun validate(item: TariffRollup): MutableList<String> {
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

    fun requireValid(item: TariffRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffRollupValidator(): TariffRollupValidator {
    return TariffRollupValidator(
        mutableListOf<TariffRollupRule>(
            TariffRollupCodePrefixRule("REF"),
            TariffRollupExternalRefLengthRule(255),
            TariffRollupAllowedDockRule(mutableListOf<String>("adaptive")),
            TariffRollupRequiredExternalRefRule("tariff rollup needs a value"),
        ),
    )
}
