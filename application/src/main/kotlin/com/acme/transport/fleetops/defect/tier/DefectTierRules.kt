package com.acme.transport.fleetops.defect.tier

import com.acme.transport.kit.text.normalize

/** A rule a defect tier must pass. Check returns its problems, or none. */
interface DefectTierRule {
    fun check(item: DefectTier): MutableList<String>
}

data class DefectTierNameLengthRule(var maxLength: Int) : DefectTierRule {
    override fun check(item: DefectTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectTierAllowedChecksumRule(var allowed: MutableList<String>) : DefectTierRule {
    override fun check(item: DefectTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DefectTierApprovedGuardRule(var limit: Int) : DefectTierRule {
    override fun check(item: DefectTier): MutableList<String> {
        if (item.approved && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DefectTierRequiredLabelRule(var message: String) : DefectTierRule {
    override fun check(item: DefectTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectTierRequiredChecksumRule(var message: String) : DefectTierRule {
    override fun check(item: DefectTier): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectTierRetriesRangeRule(var min: Int, var max: Int) : DefectTierRule {
    override fun check(item: DefectTier): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a defect tier. */
data class DefectTierValidator(var rules: MutableList<DefectTierRule>) {
    fun validate(item: DefectTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectTier>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectTierValidator(): DefectTierValidator {
    return DefectTierValidator(
        mutableListOf<DefectTierRule>(
            DefectTierNameLengthRule(64),
            DefectTierAllowedChecksumRule(mutableListOf<String>()),
            DefectTierApprovedGuardRule(255),
            DefectTierRequiredLabelRule("defect tier needs a value"),
            DefectTierRequiredChecksumRule("defect tier needs this field"),
            DefectTierRetriesRangeRule(64, 86400),
        ),
    )
}
