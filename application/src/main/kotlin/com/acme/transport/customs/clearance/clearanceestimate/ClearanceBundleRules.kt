package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.text.normalize

/** A rule a clearance bundle must pass. Check returns its problems, or none. */
interface ClearanceBundleRule {
    fun check(item: ClearanceBundle): MutableList<String>
}

data class ClearanceBundleRequiredCodeRule(var message: String) : ClearanceBundleRule {
    override fun check(item: ClearanceBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBundleAllowedTrailerIdRule(var allowed: MutableList<String>) : ClearanceBundleRule {
    override fun check(item: ClearanceBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBundleReviewerLengthRule(var maxLength: Int) : ClearanceBundleRule {
    override fun check(item: ClearanceBundle): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBundleCodePrefixRule(var prefix: String) : ClearanceBundleRule {
    override fun check(item: ClearanceBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance bundle. */
data class ClearanceBundleValidator(var rules: MutableList<ClearanceBundleRule>) {
    fun validate(item: ClearanceBundle): MutableList<String> {
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

    fun requireValid(item: ClearanceBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceBundleValidator(): ClearanceBundleValidator {
    return ClearanceBundleValidator(
        mutableListOf<ClearanceBundleRule>(
            ClearanceBundleRequiredCodeRule("clearance bundle needs this field"),
            ClearanceBundleAllowedTrailerIdRule(mutableListOf<String>("manual", "default", "global")),
            ClearanceBundleReviewerLengthRule(32),
            ClearanceBundleCodePrefixRule("OPS"),
        ),
    )
}
