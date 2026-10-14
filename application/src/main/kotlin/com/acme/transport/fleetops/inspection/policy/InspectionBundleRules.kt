package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.text.normalize

/** A rule a inspection bundle must pass. Check returns its problems, or none. */
interface InspectionBundleRule {
    fun check(item: InspectionBundle): MutableList<String>
}

data class InspectionBundleCodePrefixRule(var prefix: String) : InspectionBundleRule {
    override fun check(item: InspectionBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBundleCodeLengthRule(var maxLength: Int) : InspectionBundleRule {
    override fun check(item: InspectionBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBundleRequiredSegmentRule(var message: String) : InspectionBundleRule {
    override fun check(item: InspectionBundle): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionBundleRequiredNameRule(var message: String) : InspectionBundleRule {
    override fun check(item: InspectionBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionBundleAllowedSegmentRule(var allowed: MutableList<String>) : InspectionBundleRule {
    override fun check(item: InspectionBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBundleShiftCodeLengthRule(var maxLength: Int) : InspectionBundleRule {
    override fun check(item: InspectionBundle): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection bundle. */
data class InspectionBundleValidator(var rules: MutableList<InspectionBundleRule>) {
    fun validate(item: InspectionBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionBundleValidator(): InspectionBundleValidator {
    return InspectionBundleValidator(
        mutableListOf<InspectionBundleRule>(
            InspectionBundleCodePrefixRule("X"),
            InspectionBundleCodeLengthRule(64),
            InspectionBundleRequiredSegmentRule("inspection bundle needs this field"),
            InspectionBundleRequiredNameRule("inspection bundle needs an entry"),
            InspectionBundleAllowedSegmentRule(mutableListOf<String>("weighted", "manual", "pending")),
            InspectionBundleShiftCodeLengthRule(255),
        ),
    )
}
