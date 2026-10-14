package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.text.normalize

/** A rule a coaching bundle must pass. Check returns its problems, or none. */
interface CoachingBundleRule {
    fun check(item: CoachingBundle): MutableList<String>
}

data class CoachingBundleNameLengthRule(var maxLength: Int) : CoachingBundleRule {
    override fun check(item: CoachingBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBundleCodeLengthRule(var maxLength: Int) : CoachingBundleRule {
    override fun check(item: CoachingBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBundleRequiredCodeRule(var message: String) : CoachingBundleRule {
    override fun check(item: CoachingBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingBundleAllowedLabelRule(var allowed: MutableList<String>) : CoachingBundleRule {
    override fun check(item: CoachingBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBundleRequiredNameRule(var message: String) : CoachingBundleRule {
    override fun check(item: CoachingBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingBundleLabelLengthRule(var maxLength: Int) : CoachingBundleRule {
    override fun check(item: CoachingBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching bundle. */
data class CoachingBundleValidator(var rules: MutableList<CoachingBundleRule>) {
    fun validate(item: CoachingBundle): MutableList<String> {
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

    fun requireValid(item: CoachingBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingBundleValidator(): CoachingBundleValidator {
    return CoachingBundleValidator(
        mutableListOf<CoachingBundleRule>(
            CoachingBundleNameLengthRule(16),
            CoachingBundleCodeLengthRule(120),
            CoachingBundleRequiredCodeRule("coaching bundle needs a value"),
            CoachingBundleAllowedLabelRule(mutableListOf<String>("seasonal", "regional", "manual")),
            CoachingBundleRequiredNameRule("coaching bundle needs an entry"),
            CoachingBundleLabelLengthRule(80),
        ),
    )
}
