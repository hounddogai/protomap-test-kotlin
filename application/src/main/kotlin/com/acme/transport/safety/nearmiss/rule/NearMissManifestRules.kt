package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.text.normalize

/** A rule a near miss manifest must pass. Check returns its problems, or none. */
interface NearMissManifestRule {
    fun check(item: NearMissManifest): MutableList<String>
}

data class NearMissManifestRequiredNameRule(var message: String) : NearMissManifestRule {
    override fun check(item: NearMissManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissManifestMarginRangeRule(var min: Double, var max: Double) : NearMissManifestRule {
    override fun check(item: NearMissManifest): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class NearMissManifestCodeLengthRule(var maxLength: Int) : NearMissManifestRule {
    override fun check(item: NearMissManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissManifestAllowedDescriptionRule(var allowed: MutableList<String>) : NearMissManifestRule {
    override fun check(item: NearMissManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss manifest. */
data class NearMissManifestValidator(var rules: MutableList<NearMissManifestRule>) {
    fun validate(item: NearMissManifest): MutableList<String> {
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

    fun requireValid(item: NearMissManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissManifestValidator(): NearMissManifestValidator {
    return NearMissManifestValidator(
        mutableListOf<NearMissManifestRule>(
            NearMissManifestRequiredNameRule("near miss manifest needs an entry"),
            NearMissManifestMarginRangeRule(7.1, 3564.0),
            NearMissManifestCodeLengthRule(64),
            NearMissManifestAllowedDescriptionRule(mutableListOf<String>()),
        ),
    )
}
