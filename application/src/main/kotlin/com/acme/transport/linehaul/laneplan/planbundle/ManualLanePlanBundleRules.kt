package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.text.normalize

/** A rule a manual lane plan bundle must pass. Check returns its problems, or none. */
interface ManualLanePlanBundleRule {
    fun check(item: ManualLanePlanBundle): MutableList<String>
}

data class ManualLanePlanBundleCodePrefixRule(var prefix: String) : ManualLanePlanBundleRule {
    override fun check(item: ManualLanePlanBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualLanePlanBundleRequiredSummaryRule(var message: String) : ManualLanePlanBundleRule {
    override fun check(item: ManualLanePlanBundle): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualLanePlanBundleSummaryLengthRule(var maxLength: Int) : ManualLanePlanBundleRule {
    override fun check(item: ManualLanePlanBundle): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualLanePlanBundleLabelLengthRule(var maxLength: Int) : ManualLanePlanBundleRule {
    override fun check(item: ManualLanePlanBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualLanePlanBundleAllowedSummaryRule(var allowed: MutableList<String>) : ManualLanePlanBundleRule {
    override fun check(item: ManualLanePlanBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ManualLanePlanBundleDepotLengthRule(var maxLength: Int) : ManualLanePlanBundleRule {
    override fun check(item: ManualLanePlanBundle): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual lane plan bundle. */
data class ManualLanePlanBundleValidator(var rules: MutableList<ManualLanePlanBundleRule>) {
    fun validate(item: ManualLanePlanBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualLanePlanBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualLanePlanBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualLanePlanBundleValidator(): ManualLanePlanBundleValidator {
    return ManualLanePlanBundleValidator(
        mutableListOf<ManualLanePlanBundleRule>(
            ManualLanePlanBundleCodePrefixRule("REF"),
            ManualLanePlanBundleRequiredSummaryRule("manual lane plan bundle needs this field"),
            ManualLanePlanBundleSummaryLengthRule(120),
            ManualLanePlanBundleLabelLengthRule(3600),
            ManualLanePlanBundleAllowedSummaryRule(mutableListOf<String>()),
            ManualLanePlanBundleDepotLengthRule(3600),
        ),
    )
}
