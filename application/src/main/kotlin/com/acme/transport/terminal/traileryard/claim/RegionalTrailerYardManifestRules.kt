package com.acme.transport.terminal.traileryard.claim

/** A rule a regional trailer yard manifest must pass. Check returns its problems, or none. */
interface RegionalTrailerYardManifestRule {
    fun check(item: RegionalTrailerYardManifest): MutableList<String>
}

data class RegionalTrailerYardManifestLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RegionalTrailerYardManifestRule {
    override fun check(item: RegionalTrailerYardManifest): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class RegionalTrailerYardManifestSegmentLengthRule(var maxLength: Int) : RegionalTrailerYardManifestRule {
    override fun check(item: RegionalTrailerYardManifest): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardManifestCodeLengthRule(var maxLength: Int) : RegionalTrailerYardManifestRule {
    override fun check(item: RegionalTrailerYardManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardManifestLabelLengthRule(var maxLength: Int) : RegionalTrailerYardManifestRule {
    override fun check(item: RegionalTrailerYardManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardManifestRequiredSegmentRule(
    var message: String,
) : RegionalTrailerYardManifestRule {
    override fun check(item: RegionalTrailerYardManifest): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardManifestSummaryLengthRule(var maxLength: Int) : RegionalTrailerYardManifestRule {
    override fun check(item: RegionalTrailerYardManifest): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer yard manifest. */
data class RegionalTrailerYardManifestValidator(var rules: MutableList<RegionalTrailerYardManifestRule>) {
    fun validate(item: RegionalTrailerYardManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTrailerYardManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerYardManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerYardManifestValidator(): RegionalTrailerYardManifestValidator {
    return RegionalTrailerYardManifestValidator(
        mutableListOf<RegionalTrailerYardManifestRule>(
            RegionalTrailerYardManifestLoadKgRangeRule(8.7, 4442.3),
            RegionalTrailerYardManifestSegmentLengthRule(80),
            RegionalTrailerYardManifestCodeLengthRule(64),
            RegionalTrailerYardManifestLabelLengthRule(80),
            RegionalTrailerYardManifestRequiredSegmentRule("regional trailer yard manifest needs this field"),
            RegionalTrailerYardManifestSummaryLengthRule(3600),
        ),
    )
}
