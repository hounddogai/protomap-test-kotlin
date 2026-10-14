package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.text.normalize

/** A rule a lane plan record must pass. Check returns its problems, or none. */
interface LanePlanRecordRule {
    fun check(item: LanePlanRecord): MutableList<String>
}

data class LanePlanRecordAllowedCodeRule(var allowed: MutableList<String>) : LanePlanRecordRule {
    override fun check(item: LanePlanRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanRecordTitleLengthRule(var maxLength: Int) : LanePlanRecordRule {
    override fun check(item: LanePlanRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanRecordCodeLengthRule(var maxLength: Int) : LanePlanRecordRule {
    override fun check(item: LanePlanRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan record. */
data class LanePlanRecordValidator(var rules: MutableList<LanePlanRecordRule>) {
    fun validate(item: LanePlanRecord): MutableList<String> {
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

    fun requireValid(item: LanePlanRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanRecordValidator(): LanePlanRecordValidator {
    return LanePlanRecordValidator(
        mutableListOf<LanePlanRecordRule>(
            LanePlanRecordAllowedCodeRule(mutableListOf<String>()),
            LanePlanRecordTitleLengthRule(16),
            LanePlanRecordCodeLengthRule(86400),
        ),
    )
}
