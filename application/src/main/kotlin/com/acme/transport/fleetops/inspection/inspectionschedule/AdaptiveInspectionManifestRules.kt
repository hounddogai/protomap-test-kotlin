package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.normalize

/** A rule a adaptive inspection manifest must pass. Check returns its problems, or none. */
interface AdaptiveInspectionManifestRule {
    fun check(item: AdaptiveInspectionManifest): MutableList<String>
}

data class AdaptiveInspectionManifestRequiredTitleRule(var message: String) : AdaptiveInspectionManifestRule {
    override fun check(item: AdaptiveInspectionManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionManifestTitleLengthRule(var maxLength: Int) : AdaptiveInspectionManifestRule {
    override fun check(item: AdaptiveInspectionManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionManifestSourceLengthRule(var maxLength: Int) : AdaptiveInspectionManifestRule {
    override fun check(item: AdaptiveInspectionManifest): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionManifestAllowedTitleRule(
    var allowed: MutableList<String>,
) : AdaptiveInspectionManifestRule {
    override fun check(item: AdaptiveInspectionManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionManifestOwnerIdLengthRule(var maxLength: Int) : AdaptiveInspectionManifestRule {
    override fun check(item: AdaptiveInspectionManifest): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection manifest. */
data class AdaptiveInspectionManifestValidator(var rules: MutableList<AdaptiveInspectionManifestRule>) {
    fun validate(item: AdaptiveInspectionManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveInspectionManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionManifestValidator(): AdaptiveInspectionManifestValidator {
    return AdaptiveInspectionManifestValidator(
        mutableListOf<AdaptiveInspectionManifestRule>(
            AdaptiveInspectionManifestRequiredTitleRule("adaptive inspection manifest needs an entry"),
            AdaptiveInspectionManifestTitleLengthRule(255),
            AdaptiveInspectionManifestSourceLengthRule(32),
            AdaptiveInspectionManifestAllowedTitleRule(mutableListOf<String>("manual")),
            AdaptiveInspectionManifestOwnerIdLengthRule(255),
        ),
    )
}
