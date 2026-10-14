package com.acme.transport.linehaul.relay.manifest

import com.acme.transport.kit.text.normalize

/** A rule a relay manifest must pass. Check returns its problems, or none. */
interface RelayManifestRule {
    fun check(item: RelayManifest): MutableList<String>
}

data class RelayManifestAllowedContactEmailRule(var allowed: MutableList<String>) : RelayManifestRule {
    override fun check(item: RelayManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RelayManifestRequiredContactEmailRule(var message: String) : RelayManifestRule {
    override fun check(item: RelayManifest): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayManifestCodeLengthRule(var maxLength: Int) : RelayManifestRule {
    override fun check(item: RelayManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayManifestLoadKgRangeRule(var min: Double, var max: Double) : RelayManifestRule {
    override fun check(item: RelayManifest): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay manifest. */
data class RelayManifestValidator(var rules: MutableList<RelayManifestRule>) {
    fun validate(item: RelayManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayManifestValidator(): RelayManifestValidator {
    return RelayManifestValidator(
        mutableListOf<RelayManifestRule>(
            RelayManifestAllowedContactEmailRule(mutableListOf<String>("regional", "rolling")),
            RelayManifestRequiredContactEmailRule("relay manifest needs a value"),
            RelayManifestCodeLengthRule(64),
            RelayManifestLoadKgRangeRule(7.2, 904.5),
        ),
    )
}
