package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a idling manifest must pass. Check returns its problems, or none. */
interface IdlingManifestRule {
    fun check(item: IdlingManifest): MutableList<String>
}

data class IdlingManifestChecksumLengthRule(var maxLength: Int) : IdlingManifestRule {
    override fun check(item: IdlingManifest): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingManifestActiveGuardRule(var limit: Int) : IdlingManifestRule {
    override fun check(item: IdlingManifest): MutableList<String> {
        if (item.active && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class IdlingManifestAllowedChecksumRule(var allowed: MutableList<String>) : IdlingManifestRule {
    override fun check(item: IdlingManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class IdlingManifestCodeLengthRule(var maxLength: Int) : IdlingManifestRule {
    override fun check(item: IdlingManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling manifest. */
data class IdlingManifestValidator(var rules: MutableList<IdlingManifestRule>) {
    fun validate(item: IdlingManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingManifestValidator(): IdlingManifestValidator {
    return IdlingManifestValidator(
        mutableListOf<IdlingManifestRule>(
            IdlingManifestChecksumLengthRule(3600),
            IdlingManifestActiveGuardRule(120),
            IdlingManifestAllowedChecksumRule(mutableListOf<String>("lenient", "rolling", "primary")),
            IdlingManifestCodeLengthRule(80),
        ),
    )
}
