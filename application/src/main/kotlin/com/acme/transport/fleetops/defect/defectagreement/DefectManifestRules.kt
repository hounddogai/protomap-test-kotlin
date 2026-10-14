package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.normalize

/** A rule a defect manifest must pass. Check returns its problems, or none. */
interface DefectManifestRule {
    fun check(item: DefectManifest): MutableList<String>
}

data class DefectManifestRequiredCodeRule(var message: String) : DefectManifestRule {
    override fun check(item: DefectManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectManifestExpiresAtRangeRule(var min: Int, var max: Int) : DefectManifestRule {
    override fun check(item: DefectManifest): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class DefectManifestAllowedCodeRule(var allowed: MutableList<String>) : DefectManifestRule {
    override fun check(item: DefectManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DefectManifestDriverIdLengthRule(var maxLength: Int) : DefectManifestRule {
    override fun check(item: DefectManifest): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectManifestRequiredChecksumRule(var message: String) : DefectManifestRule {
    override fun check(item: DefectManifest): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect manifest. */
data class DefectManifestValidator(var rules: MutableList<DefectManifestRule>) {
    fun validate(item: DefectManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectManifestValidator(): DefectManifestValidator {
    return DefectManifestValidator(
        mutableListOf<DefectManifestRule>(
            DefectManifestRequiredCodeRule("defect manifest needs this field"),
            DefectManifestExpiresAtRangeRule(120, 255),
            DefectManifestAllowedCodeRule(mutableListOf<String>("capped", "adaptive", "global")),
            DefectManifestDriverIdLengthRule(1000),
            DefectManifestRequiredChecksumRule("defect manifest needs this field"),
        ),
    )
}
