package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.text.normalize

/** A rule a inspection manifest must pass. Check returns its problems, or none. */
interface InspectionManifestRule {
    fun check(item: InspectionManifest): MutableList<String>
}

data class InspectionManifestAllowedSegmentRule(var allowed: MutableList<String>) : InspectionManifestRule {
    override fun check(item: InspectionManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class InspectionManifestCodePrefixRule(var prefix: String) : InspectionManifestRule {
    override fun check(item: InspectionManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionManifestNameLengthRule(var maxLength: Int) : InspectionManifestRule {
    override fun check(item: InspectionManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionManifestRequiredNameRule(var message: String) : InspectionManifestRule {
    override fun check(item: InspectionManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection manifest. */
data class InspectionManifestValidator(var rules: MutableList<InspectionManifestRule>) {
    fun validate(item: InspectionManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionManifestValidator(): InspectionManifestValidator {
    return InspectionManifestValidator(
        mutableListOf<InspectionManifestRule>(
            InspectionManifestAllowedSegmentRule(mutableListOf<String>()),
            InspectionManifestCodePrefixRule("Q"),
            InspectionManifestNameLengthRule(255),
            InspectionManifestRequiredNameRule("inspection manifest needs a value"),
        ),
    )
}
