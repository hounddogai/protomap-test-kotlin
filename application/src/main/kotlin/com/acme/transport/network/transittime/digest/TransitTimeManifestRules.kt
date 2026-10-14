package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.normalize

/** A rule a transit time manifest must pass. Check returns its problems, or none. */
interface TransitTimeManifestRule {
    fun check(item: TransitTimeManifest): MutableList<String>
}

data class TransitTimeManifestAllowedNotesRule(var allowed: MutableList<String>) : TransitTimeManifestRule {
    override fun check(item: TransitTimeManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is expired")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeManifestNotesLengthRule(var maxLength: Int) : TransitTimeManifestRule {
    override fun check(item: TransitTimeManifest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeManifestCodeLengthRule(var maxLength: Int) : TransitTimeManifestRule {
    override fun check(item: TransitTimeManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time manifest. */
data class TransitTimeManifestValidator(var rules: MutableList<TransitTimeManifestRule>) {
    fun validate(item: TransitTimeManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeManifestValidator(): TransitTimeManifestValidator {
    return TransitTimeManifestValidator(
        mutableListOf<TransitTimeManifestRule>(
            TransitTimeManifestAllowedNotesRule(mutableListOf<String>("adaptive")),
            TransitTimeManifestNotesLengthRule(1000),
            TransitTimeManifestCodeLengthRule(120),
        ),
    )
}
