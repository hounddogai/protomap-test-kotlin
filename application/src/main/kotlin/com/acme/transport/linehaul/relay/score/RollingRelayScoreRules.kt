package com.acme.transport.linehaul.relay.score

import com.acme.transport.kit.text.normalize

/** A rule a rolling relay score must pass. Check returns its problems, or none. */
interface RollingRelayScoreRule {
    fun check(item: RollingRelayScore): MutableList<String>
}

data class RollingRelayScoreCodeLengthRule(var maxLength: Int) : RollingRelayScoreRule {
    override fun check(item: RollingRelayScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayScoreNameLengthRule(var maxLength: Int) : RollingRelayScoreRule {
    override fun check(item: RollingRelayScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayScoreAllowedTrailerIdRule(var allowed: MutableList<String>) : RollingRelayScoreRule {
    override fun check(item: RollingRelayScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayScoreRequiredLabelRule(var message: String) : RollingRelayScoreRule {
    override fun check(item: RollingRelayScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling relay score. */
data class RollingRelayScoreValidator(var rules: MutableList<RollingRelayScoreRule>) {
    fun validate(item: RollingRelayScore): MutableList<String> {
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

    fun requireValid(item: RollingRelayScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRelayScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRelayScoreValidator(): RollingRelayScoreValidator {
    return RollingRelayScoreValidator(
        mutableListOf<RollingRelayScoreRule>(
            RollingRelayScoreCodeLengthRule(80),
            RollingRelayScoreNameLengthRule(16),
            RollingRelayScoreAllowedTrailerIdRule(mutableListOf<String>()),
            RollingRelayScoreRequiredLabelRule("rolling relay score needs an entry"),
        ),
    )
}
