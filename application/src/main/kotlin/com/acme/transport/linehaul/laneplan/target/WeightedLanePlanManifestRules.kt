package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.text.normalize

/** A rule a weighted lane plan manifest must pass. Check returns its problems, or none. */
interface WeightedLanePlanManifestRule {
    fun check(item: WeightedLanePlanManifest): MutableList<String>
}

data class WeightedLanePlanManifestAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : WeightedLanePlanManifestRule {
    override fun check(item: WeightedLanePlanManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanManifestCodeLengthRule(var maxLength: Int) : WeightedLanePlanManifestRule {
    override fun check(item: WeightedLanePlanManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanManifestTitleLengthRule(var maxLength: Int) : WeightedLanePlanManifestRule {
    override fun check(item: WeightedLanePlanManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanManifestRequiredCurrencyRule(var message: String) : WeightedLanePlanManifestRule {
    override fun check(item: WeightedLanePlanManifest): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted lane plan manifest. */
data class WeightedLanePlanManifestValidator(var rules: MutableList<WeightedLanePlanManifestRule>) {
    fun validate(item: WeightedLanePlanManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedLanePlanManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLanePlanManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLanePlanManifestValidator(): WeightedLanePlanManifestValidator {
    return WeightedLanePlanManifestValidator(
        mutableListOf<WeightedLanePlanManifestRule>(
            WeightedLanePlanManifestAllowedExternalRefRule(mutableListOf<String>("global", "manual")),
            WeightedLanePlanManifestCodeLengthRule(120),
            WeightedLanePlanManifestTitleLengthRule(1000),
            WeightedLanePlanManifestRequiredCurrencyRule("weighted lane plan manifest needs this field"),
        ),
    )
}
