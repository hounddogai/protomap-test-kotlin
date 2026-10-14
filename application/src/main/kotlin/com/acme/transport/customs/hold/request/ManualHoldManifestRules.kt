package com.acme.transport.customs.hold.request

import com.acme.transport.kit.text.normalize

/** A rule a manual hold manifest must pass. Check returns its problems, or none. */
interface ManualHoldManifestRule {
    fun check(item: ManualHoldManifest): MutableList<String>
}

data class ManualHoldManifestRequiredTitleRule(var message: String) : ManualHoldManifestRule {
    override fun check(item: ManualHoldManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldManifestNotesLengthRule(var maxLength: Int) : ManualHoldManifestRule {
    override fun check(item: ManualHoldManifest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldManifestRequiredNotesRule(var message: String) : ManualHoldManifestRule {
    override fun check(item: ManualHoldManifest): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldManifestNameLengthRule(var maxLength: Int) : ManualHoldManifestRule {
    override fun check(item: ManualHoldManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldManifestAllowedCodeRule(var allowed: MutableList<String>) : ManualHoldManifestRule {
    override fun check(item: ManualHoldManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldManifestCodePrefixRule(var prefix: String) : ManualHoldManifestRule {
    override fun check(item: ManualHoldManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual hold manifest. */
data class ManualHoldManifestValidator(var rules: MutableList<ManualHoldManifestRule>) {
    fun validate(item: ManualHoldManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualHoldManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualHoldManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualHoldManifestValidator(): ManualHoldManifestValidator {
    return ManualHoldManifestValidator(
        mutableListOf<ManualHoldManifestRule>(
            ManualHoldManifestRequiredTitleRule("manual hold manifest needs a value"),
            ManualHoldManifestNotesLengthRule(255),
            ManualHoldManifestRequiredNotesRule("manual hold manifest needs a value"),
            ManualHoldManifestNameLengthRule(32),
            ManualHoldManifestAllowedCodeRule(mutableListOf<String>("global")),
            ManualHoldManifestCodePrefixRule("Q"),
        ),
    )
}
