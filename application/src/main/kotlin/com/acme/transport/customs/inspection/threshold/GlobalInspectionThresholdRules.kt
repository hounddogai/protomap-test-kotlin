package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.text.normalize

/** A rule a global inspection threshold must pass. Check returns its problems, or none. */
interface GlobalInspectionThresholdRule {
    fun check(item: GlobalInspectionThreshold): MutableList<String>
}

data class GlobalInspectionThresholdCodePrefixRule(var prefix: String) : GlobalInspectionThresholdRule {
    override fun check(item: GlobalInspectionThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionThresholdAllowedDepotRule(
    var allowed: MutableList<String>,
) : GlobalInspectionThresholdRule {
    override fun check(item: GlobalInspectionThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is missing")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionThresholdRequiredLaneRule(var message: String) : GlobalInspectionThresholdRule {
    override fun check(item: GlobalInspectionThreshold): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionThresholdCodeLengthRule(var maxLength: Int) : GlobalInspectionThresholdRule {
    override fun check(item: GlobalInspectionThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionThresholdRequiredDepotRule(var message: String) : GlobalInspectionThresholdRule {
    override fun check(item: GlobalInspectionThreshold): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionThresholdTitleLengthRule(var maxLength: Int) : GlobalInspectionThresholdRule {
    override fun check(item: GlobalInspectionThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global inspection threshold. */
data class GlobalInspectionThresholdValidator(var rules: MutableList<GlobalInspectionThresholdRule>) {
    fun validate(item: GlobalInspectionThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalInspectionThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalInspectionThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalInspectionThresholdValidator(): GlobalInspectionThresholdValidator {
    return GlobalInspectionThresholdValidator(
        mutableListOf<GlobalInspectionThresholdRule>(
            GlobalInspectionThresholdCodePrefixRule("ACM"),
            GlobalInspectionThresholdAllowedDepotRule(mutableListOf<String>("strict", "lenient", "lenient")),
            GlobalInspectionThresholdRequiredLaneRule("global inspection threshold needs an entry"),
            GlobalInspectionThresholdCodeLengthRule(3600),
            GlobalInspectionThresholdRequiredDepotRule("global inspection threshold needs a value"),
            GlobalInspectionThresholdTitleLengthRule(1000),
        ),
    )
}
