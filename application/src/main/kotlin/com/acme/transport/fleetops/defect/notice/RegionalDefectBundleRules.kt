package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.text.normalize

/** A rule a regional defect bundle must pass. Check returns its problems, or none. */
interface RegionalDefectBundleRule {
    fun check(item: RegionalDefectBundle): MutableList<String>
}

data class RegionalDefectBundleRequiredDriverIdRule(var message: String) : RegionalDefectBundleRule {
    override fun check(item: RegionalDefectBundle): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectBundleCodeLengthRule(var maxLength: Int) : RegionalDefectBundleRule {
    override fun check(item: RegionalDefectBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectBundleRequiredLabelRule(var message: String) : RegionalDefectBundleRule {
    override fun check(item: RegionalDefectBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectBundleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RegionalDefectBundleRule {
    override fun check(item: RegionalDefectBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RegionalDefectBundleRequiredCarrierRule(var message: String) : RegionalDefectBundleRule {
    override fun check(item: RegionalDefectBundle): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectBundleAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : RegionalDefectBundleRule {
    override fun check(item: RegionalDefectBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional defect bundle. */
data class RegionalDefectBundleValidator(var rules: MutableList<RegionalDefectBundleRule>) {
    fun validate(item: RegionalDefectBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDefectBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDefectBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDefectBundleValidator(): RegionalDefectBundleValidator {
    return RegionalDefectBundleValidator(
        mutableListOf<RegionalDefectBundleRule>(
            RegionalDefectBundleRequiredDriverIdRule("regional defect bundle needs an entry"),
            RegionalDefectBundleCodeLengthRule(80),
            RegionalDefectBundleRequiredLabelRule("regional defect bundle needs this field"),
            RegionalDefectBundleTagsLimitRule(86400, mutableListOf<String>("legacy", "primary")),
            RegionalDefectBundleRequiredCarrierRule("regional defect bundle needs an entry"),
            RegionalDefectBundleAllowedDriverIdRule(mutableListOf<String>()),
        ),
    )
}
