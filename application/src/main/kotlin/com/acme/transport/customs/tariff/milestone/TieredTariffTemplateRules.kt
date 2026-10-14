package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.text.normalize

/** A rule a tiered tariff template must pass. Check returns its problems, or none. */
interface TieredTariffTemplateRule {
    fun check(item: TieredTariffTemplate): MutableList<String>
}

data class TieredTariffTemplateCodePrefixRule(var prefix: String) : TieredTariffTemplateRule {
    override fun check(item: TieredTariffTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTariffTemplateTitleLengthRule(var maxLength: Int) : TieredTariffTemplateRule {
    override fun check(item: TieredTariffTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTariffTemplateRequiredTitleRule(var message: String) : TieredTariffTemplateRule {
    override fun check(item: TieredTariffTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTariffTemplateAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : TieredTariffTemplateRule {
    override fun check(item: TieredTariffTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tariff template. */
data class TieredTariffTemplateValidator(var rules: MutableList<TieredTariffTemplateRule>) {
    fun validate(item: TieredTariffTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredTariffTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTariffTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTariffTemplateValidator(): TieredTariffTemplateValidator {
    return TieredTariffTemplateValidator(
        mutableListOf<TieredTariffTemplateRule>(
            TieredTariffTemplateCodePrefixRule("REF"),
            TieredTariffTemplateTitleLengthRule(64),
            TieredTariffTemplateRequiredTitleRule("tiered tariff template needs an entry"),
            TieredTariffTemplateAllowedRouteCodeRule(mutableListOf<String>("strict", "lenient", "adaptive")),
        ),
    )
}
