package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.text.normalize

/** A rule a seasonal backhaul manifest must pass. Check returns its problems, or none. */
interface SeasonalBackhaulManifestRule {
    fun check(item: SeasonalBackhaulManifest): MutableList<String>
}

data class SeasonalBackhaulManifestRequiredLabelRule(var message: String) : SeasonalBackhaulManifestRule {
    override fun check(item: SeasonalBackhaulManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBackhaulManifestAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalBackhaulManifestRule {
    override fun check(item: SeasonalBackhaulManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBackhaulManifestSourceLengthRule(var maxLength: Int) : SeasonalBackhaulManifestRule {
    override fun check(item: SeasonalBackhaulManifest): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBackhaulManifestCodeLengthRule(var maxLength: Int) : SeasonalBackhaulManifestRule {
    override fun check(item: SeasonalBackhaulManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal backhaul manifest. */
data class SeasonalBackhaulManifestValidator(var rules: MutableList<SeasonalBackhaulManifestRule>) {
    fun validate(item: SeasonalBackhaulManifest): MutableList<String> {
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

    fun requireValid(item: SeasonalBackhaulManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalBackhaulManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalBackhaulManifestValidator(): SeasonalBackhaulManifestValidator {
    return SeasonalBackhaulManifestValidator(
        mutableListOf<SeasonalBackhaulManifestRule>(
            SeasonalBackhaulManifestRequiredLabelRule("seasonal backhaul manifest needs this field"),
            SeasonalBackhaulManifestAllowedLabelRule(mutableListOf<String>("lenient", "adaptive", "seasonal")),
            SeasonalBackhaulManifestSourceLengthRule(86400),
            SeasonalBackhaulManifestCodeLengthRule(86400),
        ),
    )
}
