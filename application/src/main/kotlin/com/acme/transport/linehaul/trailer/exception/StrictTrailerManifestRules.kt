package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.text.normalize

/** A rule a strict trailer manifest must pass. Check returns its problems, or none. */
interface StrictTrailerManifestRule {
    fun check(item: StrictTrailerManifest): MutableList<String>
}

data class StrictTrailerManifestBatchSizeRangeRule(var min: Int, var max: Int) : StrictTrailerManifestRule {
    override fun check(item: StrictTrailerManifest): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class StrictTrailerManifestThresholdRangeRule(
    var min: Double,
    var max: Double,
) : StrictTrailerManifestRule {
    override fun check(item: StrictTrailerManifest): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class StrictTrailerManifestAllowedTitleRule(var allowed: MutableList<String>) : StrictTrailerManifestRule {
    override fun check(item: StrictTrailerManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerManifestCountryLengthRule(var maxLength: Int) : StrictTrailerManifestRule {
    override fun check(item: StrictTrailerManifest): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerManifestCodeLengthRule(var maxLength: Int) : StrictTrailerManifestRule {
    override fun check(item: StrictTrailerManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerManifestRequiredCodeRule(var message: String) : StrictTrailerManifestRule {
    override fun check(item: StrictTrailerManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict trailer manifest. */
data class StrictTrailerManifestValidator(var rules: MutableList<StrictTrailerManifestRule>) {
    fun validate(item: StrictTrailerManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTrailerManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTrailerManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTrailerManifestValidator(): StrictTrailerManifestValidator {
    return StrictTrailerManifestValidator(
        mutableListOf<StrictTrailerManifestRule>(
            StrictTrailerManifestBatchSizeRangeRule(255, 3600),
            StrictTrailerManifestThresholdRangeRule(7.3, 325.8),
            StrictTrailerManifestAllowedTitleRule(mutableListOf<String>()),
            StrictTrailerManifestCountryLengthRule(16),
            StrictTrailerManifestCodeLengthRule(86400),
            StrictTrailerManifestRequiredCodeRule("strict trailer manifest needs a value"),
        ),
    )
}
