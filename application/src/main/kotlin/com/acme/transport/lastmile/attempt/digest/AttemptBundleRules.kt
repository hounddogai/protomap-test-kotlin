package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.text.normalize

/** A rule a attempt bundle must pass. Check returns its problems, or none. */
interface AttemptBundleRule {
    fun check(item: AttemptBundle): MutableList<String>
}

data class AttemptBundlePhoneLengthRule(var maxLength: Int) : AttemptBundleRule {
    override fun check(item: AttemptBundle): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBundleAllowedCodeRule(var allowed: MutableList<String>) : AttemptBundleRule {
    override fun check(item: AttemptBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBundlePinnedGuardRule(var limit: Int) : AttemptBundleRule {
    override fun check(item: AttemptBundle): MutableList<String> {
        if (item.pinned && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBundleRequiredDepotRule(var message: String) : AttemptBundleRule {
    override fun check(item: AttemptBundle): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptBundleNameLengthRule(var maxLength: Int) : AttemptBundleRule {
    override fun check(item: AttemptBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBundleRequiredCodeRule(var message: String) : AttemptBundleRule {
    override fun check(item: AttemptBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt bundle. */
data class AttemptBundleValidator(var rules: MutableList<AttemptBundleRule>) {
    fun validate(item: AttemptBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptBundleValidator(): AttemptBundleValidator {
    return AttemptBundleValidator(
        mutableListOf<AttemptBundleRule>(
            AttemptBundlePhoneLengthRule(80),
            AttemptBundleAllowedCodeRule(mutableListOf<String>("primary")),
            AttemptBundlePinnedGuardRule(3600),
            AttemptBundleRequiredDepotRule("attempt bundle needs a value"),
            AttemptBundleNameLengthRule(32),
            AttemptBundleRequiredCodeRule("attempt bundle needs this field"),
        ),
    )
}
