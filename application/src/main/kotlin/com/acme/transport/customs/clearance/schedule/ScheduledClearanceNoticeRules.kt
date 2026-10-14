package com.acme.transport.customs.clearance.schedule

import com.acme.transport.kit.text.normalize

/** A rule a scheduled clearance notice must pass. Check returns its problems, or none. */
interface ScheduledClearanceNoticeRule {
    fun check(item: ScheduledClearanceNotice): MutableList<String>
}

data class ScheduledClearanceNoticeAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledClearanceNoticeRule {
    override fun check(item: ScheduledClearanceNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledClearanceNoticeShiftCodeLengthRule(var maxLength: Int) : ScheduledClearanceNoticeRule {
    override fun check(item: ScheduledClearanceNotice): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledClearanceNoticeRequiredCodeRule(var message: String) : ScheduledClearanceNoticeRule {
    override fun check(item: ScheduledClearanceNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledClearanceNoticeDepotLengthRule(var maxLength: Int) : ScheduledClearanceNoticeRule {
    override fun check(item: ScheduledClearanceNotice): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled clearance notice. */
data class ScheduledClearanceNoticeValidator(var rules: MutableList<ScheduledClearanceNoticeRule>) {
    fun validate(item: ScheduledClearanceNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledClearanceNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledClearanceNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledClearanceNoticeValidator(): ScheduledClearanceNoticeValidator {
    return ScheduledClearanceNoticeValidator(
        mutableListOf<ScheduledClearanceNoticeRule>(
            ScheduledClearanceNoticeAllowedCodeRule(mutableListOf<String>("legacy", "rolling")),
            ScheduledClearanceNoticeShiftCodeLengthRule(32),
            ScheduledClearanceNoticeRequiredCodeRule("scheduled clearance notice needs a value"),
            ScheduledClearanceNoticeDepotLengthRule(120),
        ),
    )
}
