package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.text.normalize

/** A rule a swap manifest must pass. Check returns its problems, or none. */
interface SwapManifestRule {
    fun check(item: SwapManifest): MutableList<String>
}

data class SwapManifestRequiredTitleRule(var message: String) : SwapManifestRule {
    override fun check(item: SwapManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapManifestTitleLengthRule(var maxLength: Int) : SwapManifestRule {
    override fun check(item: SwapManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapManifestCountryLengthRule(var maxLength: Int) : SwapManifestRule {
    override fun check(item: SwapManifest): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapManifestRequiredTrailerIdRule(var message: String) : SwapManifestRule {
    override fun check(item: SwapManifest): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapManifestAllowedTrailerIdRule(var allowed: MutableList<String>) : SwapManifestRule {
    override fun check(item: SwapManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SwapManifestActiveGuardRule(var limit: Double) : SwapManifestRule {
    override fun check(item: SwapManifest): MutableList<String> {
        if (item.active && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap manifest. */
data class SwapManifestValidator(var rules: MutableList<SwapManifestRule>) {
    fun validate(item: SwapManifest): MutableList<String> {
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

    fun requireValid(item: SwapManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapManifestValidator(): SwapManifestValidator {
    return SwapManifestValidator(
        mutableListOf<SwapManifestRule>(
            SwapManifestRequiredTitleRule("swap manifest needs a value"),
            SwapManifestTitleLengthRule(120),
            SwapManifestCountryLengthRule(80),
            SwapManifestRequiredTrailerIdRule("swap manifest needs this field"),
            SwapManifestAllowedTrailerIdRule(mutableListOf<String>("primary", "global", "weighted")),
            SwapManifestActiveGuardRule(2.3),
        ),
    )
}
