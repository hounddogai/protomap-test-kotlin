package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.text.normalize

/** A rule a regional drop segment must pass. Check returns its problems, or none. */
interface RegionalDropSegmentRule {
    fun check(item: RegionalDropSegment): MutableList<String>
}

data class RegionalDropSegmentRequiredNameRule(var message: String) : RegionalDropSegmentRule {
    override fun check(item: RegionalDropSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropSegmentRouteCodeLengthRule(var maxLength: Int) : RegionalDropSegmentRule {
    override fun check(item: RegionalDropSegment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropSegmentRequiredPhoneRule(var message: String) : RegionalDropSegmentRule {
    override fun check(item: RegionalDropSegment): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropSegmentAllowedNameRule(var allowed: MutableList<String>) : RegionalDropSegmentRule {
    override fun check(item: RegionalDropSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropSegmentNameLengthRule(var maxLength: Int) : RegionalDropSegmentRule {
    override fun check(item: RegionalDropSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropSegmentRequiredCodeRule(var message: String) : RegionalDropSegmentRule {
    override fun check(item: RegionalDropSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional drop segment. */
data class RegionalDropSegmentValidator(var rules: MutableList<RegionalDropSegmentRule>) {
    fun validate(item: RegionalDropSegment): MutableList<String> {
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

    fun requireValid(item: RegionalDropSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDropSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDropSegmentValidator(): RegionalDropSegmentValidator {
    return RegionalDropSegmentValidator(
        mutableListOf<RegionalDropSegmentRule>(
            RegionalDropSegmentRequiredNameRule("regional drop segment needs a value"),
            RegionalDropSegmentRouteCodeLengthRule(64),
            RegionalDropSegmentRequiredPhoneRule("regional drop segment needs an entry"),
            RegionalDropSegmentAllowedNameRule(mutableListOf<String>("scheduled", "regional")),
            RegionalDropSegmentNameLengthRule(32),
            RegionalDropSegmentRequiredCodeRule("regional drop segment needs this field"),
        ),
    )
}
