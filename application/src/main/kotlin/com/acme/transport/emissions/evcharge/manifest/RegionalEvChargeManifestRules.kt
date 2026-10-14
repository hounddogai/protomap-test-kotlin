package com.acme.transport.emissions.evcharge.manifest

/** A rule a regional ev charge manifest must pass. Check returns its problems, or none. */
interface RegionalEvChargeManifestRule {
    fun check(item: RegionalEvChargeManifest): MutableList<String>
}

data class RegionalEvChargeManifestRevisionRangeRule(
    var min: Int,
    var max: Int,
) : RegionalEvChargeManifestRule {
    override fun check(item: RegionalEvChargeManifest): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeManifestLabelLengthRule(var maxLength: Int) : RegionalEvChargeManifestRule {
    override fun check(item: RegionalEvChargeManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeManifestCodeLengthRule(var maxLength: Int) : RegionalEvChargeManifestRule {
    override fun check(item: RegionalEvChargeManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional ev charge manifest. */
data class RegionalEvChargeManifestValidator(var rules: MutableList<RegionalEvChargeManifestRule>) {
    fun validate(item: RegionalEvChargeManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalEvChargeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalEvChargeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalEvChargeManifestValidator(): RegionalEvChargeManifestValidator {
    return RegionalEvChargeManifestValidator(
        mutableListOf<RegionalEvChargeManifestRule>(
            RegionalEvChargeManifestRevisionRangeRule(64, 255),
            RegionalEvChargeManifestLabelLengthRule(3600),
            RegionalEvChargeManifestCodeLengthRule(80),
        ),
    )
}
