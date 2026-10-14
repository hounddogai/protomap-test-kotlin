package com.acme.transport.fleetops.tire.preference

/** A rule a tire preference must pass. Check returns its problems, or none. */
interface TirePreferenceRule {
    fun check(item: TirePreference): MutableList<String>
}

data class TirePreferenceCodeLengthRule(var maxLength: Int) : TirePreferenceRule {
    override fun check(item: TirePreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TirePreferenceRequiredLabelRule(var message: String) : TirePreferenceRule {
    override fun check(item: TirePreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TirePreferenceRequiredCodeRule(var message: String) : TirePreferenceRule {
    override fun check(item: TirePreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TirePreferenceTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : TirePreferenceRule {
    override fun check(item: TirePreference): MutableList<String> {
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

/** Runs every rule of a tire preference. */
data class TirePreferenceValidator(var rules: MutableList<TirePreferenceRule>) {
    fun validate(item: TirePreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TirePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TirePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultTirePreferenceValidator(): TirePreferenceValidator {
    return TirePreferenceValidator(
        mutableListOf<TirePreferenceRule>(
            TirePreferenceCodeLengthRule(86400),
            TirePreferenceRequiredLabelRule("tire preference needs a value"),
            TirePreferenceRequiredCodeRule("tire preference needs a value"),
            TirePreferenceTagsLimitRule(255, mutableListOf<String>("default")),
        ),
    )
}
