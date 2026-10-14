package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.text.normalize

/** A rule a defect rule must pass. Check returns its problems, or none. */
interface DefectRuleRule {
    fun check(item: DefectRule): MutableList<String>
}

data class DefectRuleCodeLengthRule(var maxLength: Int) : DefectRuleRule {
    override fun check(item: DefectRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectRuleEtaMinutesRangeRule(var min: Int, var max: Int) : DefectRuleRule {
    override fun check(item: DefectRule): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class DefectRuleChannelLengthRule(var maxLength: Int) : DefectRuleRule {
    override fun check(item: DefectRule): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectRuleAllowedLabelRule(var allowed: MutableList<String>) : DefectRuleRule {
    override fun check(item: DefectRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DefectRuleRequiredDepotRule(var message: String) : DefectRuleRule {
    override fun check(item: DefectRule): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectRuleTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : DefectRuleRule {
    override fun check(item: DefectRule): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a defect rule. */
data class DefectRuleValidator(var rules: MutableList<DefectRuleRule>) {
    fun validate(item: DefectRule): MutableList<String> {
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

    fun requireValid(item: DefectRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectRuleValidator(): DefectRuleValidator {
    return DefectRuleValidator(
        mutableListOf<DefectRuleRule>(
            DefectRuleCodeLengthRule(86400),
            DefectRuleEtaMinutesRangeRule(80, 255),
            DefectRuleChannelLengthRule(120),
            DefectRuleAllowedLabelRule(mutableListOf<String>()),
            DefectRuleRequiredDepotRule("defect rule needs an entry"),
            DefectRuleTagsLimitRule(80, mutableListOf<String>()),
        ),
    )
}
