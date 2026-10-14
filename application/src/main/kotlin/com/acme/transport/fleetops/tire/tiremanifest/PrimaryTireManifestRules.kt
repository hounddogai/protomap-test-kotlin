package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.normalize

/** A rule a primary tire manifest must pass. Check returns its problems, or none. */
interface PrimaryTireManifestRule {
    fun check(item: PrimaryTireManifest): MutableList<String>
}

data class PrimaryTireManifestLabelLengthRule(var maxLength: Int) : PrimaryTireManifestRule {
    override fun check(item: PrimaryTireManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireManifestAllowedLabelRule(var allowed: MutableList<String>) : PrimaryTireManifestRule {
    override fun check(item: PrimaryTireManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireManifestMinimumRangeRule(var min: Double, var max: Double) : PrimaryTireManifestRule {
    override fun check(item: PrimaryTireManifest): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireManifestCodeLengthRule(var maxLength: Int) : PrimaryTireManifestRule {
    override fun check(item: PrimaryTireManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireManifestRequiredLabelRule(var message: String) : PrimaryTireManifestRule {
    override fun check(item: PrimaryTireManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tire manifest. */
data class PrimaryTireManifestValidator(var rules: MutableList<PrimaryTireManifestRule>) {
    fun validate(item: PrimaryTireManifest): MutableList<String> {
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

    fun requireValid(item: PrimaryTireManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTireManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTireManifestValidator(): PrimaryTireManifestValidator {
    return PrimaryTireManifestValidator(
        mutableListOf<PrimaryTireManifestRule>(
            PrimaryTireManifestLabelLengthRule(255),
            PrimaryTireManifestAllowedLabelRule(mutableListOf<String>()),
            PrimaryTireManifestMinimumRangeRule(8.8, 1137.2),
            PrimaryTireManifestCodeLengthRule(255),
            PrimaryTireManifestRequiredLabelRule("primary tire manifest needs a value"),
        ),
    )
}
