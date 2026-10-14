package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.text.normalize

/** A rule a incident score must pass. Check returns its problems, or none. */
interface IncidentScoreRule {
    fun check(item: IncidentScore): MutableList<String>
}

data class IncidentScoreNameLengthRule(var maxLength: Int) : IncidentScoreRule {
    override fun check(item: IncidentScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentScoreRequiredNameRule(var message: String) : IncidentScoreRule {
    override fun check(item: IncidentScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentScoreAllowedChecksumRule(var allowed: MutableList<String>) : IncidentScoreRule {
    override fun check(item: IncidentScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class IncidentScoreCeilingRangeRule(var min: Double, var max: Double) : IncidentScoreRule {
    override fun check(item: IncidentScore): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a incident score. */
data class IncidentScoreValidator(var rules: MutableList<IncidentScoreRule>) {
    fun validate(item: IncidentScore): MutableList<String> {
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

    fun requireValid(item: IncidentScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentScore>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentScoreValidator(): IncidentScoreValidator {
    return IncidentScoreValidator(
        mutableListOf<IncidentScoreRule>(
            IncidentScoreNameLengthRule(1000),
            IncidentScoreRequiredNameRule("incident score needs an entry"),
            IncidentScoreAllowedChecksumRule(mutableListOf<String>("seasonal", "batched")),
            IncidentScoreCeilingRangeRule(9.0, 2393.4),
        ),
    )
}
