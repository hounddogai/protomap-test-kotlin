package com.acme.transport.emissions.idling.score

import com.acme.transport.kit.text.normalize

/** A rule a idling preference must pass. Check returns its problems, or none. */
interface IdlingPreferenceRule {
    fun check(item: IdlingPreference): MutableList<String>
}

data class IdlingPreferenceCodeLengthRule(var maxLength: Int) : IdlingPreferenceRule {
    override fun check(item: IdlingPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingPreferenceTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : IdlingPreferenceRule {
    override fun check(item: IdlingPreference): MutableList<String> {
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

data class IdlingPreferenceRequiredTitleRule(var message: String) : IdlingPreferenceRule {
    override fun check(item: IdlingPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingPreferenceRequiredLocaleRule(var message: String) : IdlingPreferenceRule {
    override fun check(item: IdlingPreference): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingPreferenceAllowedLocaleRule(var allowed: MutableList<String>) : IdlingPreferenceRule {
    override fun check(item: IdlingPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling preference. */
data class IdlingPreferenceValidator(var rules: MutableList<IdlingPreferenceRule>) {
    fun validate(item: IdlingPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingPreferenceValidator(): IdlingPreferenceValidator {
    return IdlingPreferenceValidator(
        mutableListOf<IdlingPreferenceRule>(
            IdlingPreferenceCodeLengthRule(80),
            IdlingPreferenceTagsLimitRule(32, mutableListOf<String>("pending")),
            IdlingPreferenceRequiredTitleRule("idling preference needs a value"),
            IdlingPreferenceRequiredLocaleRule("idling preference needs this field"),
            IdlingPreferenceAllowedLocaleRule(mutableListOf<String>()),
        ),
    )
}
