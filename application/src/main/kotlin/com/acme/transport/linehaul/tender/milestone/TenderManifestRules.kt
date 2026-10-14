package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.normalize

/** A rule a tender manifest must pass. Check returns its problems, or none. */
interface TenderManifestRule {
    fun check(item: TenderManifest): MutableList<String>
}

data class TenderManifestLabelLengthRule(var maxLength: Int) : TenderManifestRule {
    override fun check(item: TenderManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderManifestCodeLengthRule(var maxLength: Int) : TenderManifestRule {
    override fun check(item: TenderManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderManifestAllowedNameRule(var allowed: MutableList<String>) : TenderManifestRule {
    override fun check(item: TenderManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TenderManifestRequiredLabelRule(var message: String) : TenderManifestRule {
    override fun check(item: TenderManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender manifest. */
data class TenderManifestValidator(var rules: MutableList<TenderManifestRule>) {
    fun validate(item: TenderManifest): MutableList<String> {
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

    fun requireValid(item: TenderManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderManifestValidator(): TenderManifestValidator {
    return TenderManifestValidator(
        mutableListOf<TenderManifestRule>(
            TenderManifestLabelLengthRule(64),
            TenderManifestCodeLengthRule(1000),
            TenderManifestAllowedNameRule(mutableListOf<String>("rolling")),
            TenderManifestRequiredLabelRule("tender manifest needs this field"),
        ),
    )
}
