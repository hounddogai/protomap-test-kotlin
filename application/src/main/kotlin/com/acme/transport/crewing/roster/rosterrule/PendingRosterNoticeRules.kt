package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.normalize

/** A rule a pending roster notice must pass. Check returns its problems, or none. */
interface PendingRosterNoticeRule {
    fun check(item: PendingRosterNotice): MutableList<String>
}

data class PendingRosterNoticeCodeLengthRule(var maxLength: Int) : PendingRosterNoticeRule {
    override fun check(item: PendingRosterNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterNoticeAllowedDriverIdRule(var allowed: MutableList<String>) : PendingRosterNoticeRule {
    override fun check(item: PendingRosterNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterNoticeScoreRangeRule(var min: Double, var max: Double) : PendingRosterNoticeRule {
    override fun check(item: PendingRosterNotice): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterNoticeRequiredDriverIdRule(var message: String) : PendingRosterNoticeRule {
    override fun check(item: PendingRosterNotice): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterNoticeNameLengthRule(var maxLength: Int) : PendingRosterNoticeRule {
    override fun check(item: PendingRosterNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterNoticeDriverIdLengthRule(var maxLength: Int) : PendingRosterNoticeRule {
    override fun check(item: PendingRosterNotice): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending roster notice. */
data class PendingRosterNoticeValidator(var rules: MutableList<PendingRosterNoticeRule>) {
    fun validate(item: PendingRosterNotice): MutableList<String> {
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

    fun requireValid(item: PendingRosterNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRosterNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRosterNoticeValidator(): PendingRosterNoticeValidator {
    return PendingRosterNoticeValidator(
        mutableListOf<PendingRosterNoticeRule>(
            PendingRosterNoticeCodeLengthRule(120),
            PendingRosterNoticeAllowedDriverIdRule(mutableListOf<String>()),
            PendingRosterNoticeScoreRangeRule(8.4, 1173.3),
            PendingRosterNoticeRequiredDriverIdRule("pending roster notice needs a value"),
            PendingRosterNoticeNameLengthRule(1000),
            PendingRosterNoticeDriverIdLengthRule(64),
        ),
    )
}
