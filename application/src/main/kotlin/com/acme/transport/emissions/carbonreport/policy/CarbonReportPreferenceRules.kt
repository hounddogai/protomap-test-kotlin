package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.text.normalize

/** A rule a carbon report preference must pass. Check returns its problems, or none. */
interface CarbonReportPreferenceRule {
    fun check(item: CarbonReportPreference): MutableList<String>
}

data class CarbonReportPreferenceRequiredReviewerRule(var message: String) : CarbonReportPreferenceRule {
    override fun check(item: CarbonReportPreference): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportPreferenceArchivedGuardRule(var limit: Int) : CarbonReportPreferenceRule {
    override fun check(item: CarbonReportPreference): MutableList<String> {
        if (item.archived && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportPreferenceRequiredChecksumRule(var message: String) : CarbonReportPreferenceRule {
    override fun check(item: CarbonReportPreference): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportPreferenceChecksumLengthRule(var maxLength: Int) : CarbonReportPreferenceRule {
    override fun check(item: CarbonReportPreference): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportPreferenceBatchSizeRangeRule(var min: Int, var max: Int) : CarbonReportPreferenceRule {
    override fun check(item: CarbonReportPreference): MutableList<String> {
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

data class CarbonReportPreferenceAllowedLabelRule(
    var allowed: MutableList<String>,
) : CarbonReportPreferenceRule {
    override fun check(item: CarbonReportPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report preference. */
data class CarbonReportPreferenceValidator(var rules: MutableList<CarbonReportPreferenceRule>) {
    fun validate(item: CarbonReportPreference): MutableList<String> {
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

    fun requireValid(item: CarbonReportPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportPreferenceValidator(): CarbonReportPreferenceValidator {
    return CarbonReportPreferenceValidator(
        mutableListOf<CarbonReportPreferenceRule>(
            CarbonReportPreferenceRequiredReviewerRule("carbon report preference needs an entry"),
            CarbonReportPreferenceArchivedGuardRule(80),
            CarbonReportPreferenceRequiredChecksumRule("carbon report preference needs an entry"),
            CarbonReportPreferenceChecksumLengthRule(255),
            CarbonReportPreferenceBatchSizeRangeRule(3600, 3600),
            CarbonReportPreferenceAllowedLabelRule(mutableListOf<String>("scheduled", "adaptive", "pending")),
        ),
    )
}
