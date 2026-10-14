package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.kit.text.normalize

/** A rule a lane plan manifest must pass. Check returns its problems, or none. */
interface LanePlanManifestRule {
    fun check(item: LanePlanManifest): MutableList<String>
}

data class LanePlanManifestAllowedTitleRule(var allowed: MutableList<String>) : LanePlanManifestRule {
    override fun check(item: LanePlanManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanManifestEtaMinutesRangeRule(var min: Int, var max: Int) : LanePlanManifestRule {
    override fun check(item: LanePlanManifest): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanManifestCodePrefixRule(var prefix: String) : LanePlanManifestRule {
    override fun check(item: LanePlanManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanManifestRequiredTitleRule(var message: String) : LanePlanManifestRule {
    override fun check(item: LanePlanManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanManifestCodeLengthRule(var maxLength: Int) : LanePlanManifestRule {
    override fun check(item: LanePlanManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanManifestDurationMinutesRangeRule(var min: Int, var max: Int) : LanePlanManifestRule {
    override fun check(item: LanePlanManifest): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan manifest. */
data class LanePlanManifestValidator(var rules: MutableList<LanePlanManifestRule>) {
    fun validate(item: LanePlanManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanManifestValidator(): LanePlanManifestValidator {
    return LanePlanManifestValidator(
        mutableListOf<LanePlanManifestRule>(
            LanePlanManifestAllowedTitleRule(mutableListOf<String>()),
            LanePlanManifestEtaMinutesRangeRule(32, 86400),
            LanePlanManifestCodePrefixRule("REF"),
            LanePlanManifestRequiredTitleRule("lane plan manifest needs this field"),
            LanePlanManifestCodeLengthRule(16),
            LanePlanManifestDurationMinutesRangeRule(32, 80),
        ),
    )
}
