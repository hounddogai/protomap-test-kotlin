package com.acme.transport.customs.inspection.inspectiondigest

/** A rule a seasonal inspection digest must pass. Check returns its problems, or none. */
interface SeasonalInspectionDigestRule {
    fun check(item: SeasonalInspectionDigest): MutableList<String>
}

data class SeasonalInspectionDigestRequiredLabelRule(var message: String) : SeasonalInspectionDigestRule {
    override fun check(item: SeasonalInspectionDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalInspectionDigestCodePrefixRule(var prefix: String) : SeasonalInspectionDigestRule {
    override fun check(item: SeasonalInspectionDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalInspectionDigestCodeLengthRule(var maxLength: Int) : SeasonalInspectionDigestRule {
    override fun check(item: SeasonalInspectionDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal inspection digest. */
data class SeasonalInspectionDigestValidator(var rules: MutableList<SeasonalInspectionDigestRule>) {
    fun validate(item: SeasonalInspectionDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalInspectionDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalInspectionDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalInspectionDigestValidator(): SeasonalInspectionDigestValidator {
    return SeasonalInspectionDigestValidator(
        mutableListOf<SeasonalInspectionDigestRule>(
            SeasonalInspectionDigestRequiredLabelRule("seasonal inspection digest needs a value"),
            SeasonalInspectionDigestCodePrefixRule("Q"),
            SeasonalInspectionDigestCodeLengthRule(64),
        ),
    )
}
