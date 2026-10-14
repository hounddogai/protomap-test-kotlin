package com.acme.transport.lastmile.locker.lockerrule

/** A rule a scheduled locker rule must pass. Check returns its problems, or none. */
interface ScheduledLockerRuleRule {
    fun check(item: ScheduledLockerRule): MutableList<String>
}

data class ScheduledLockerRuleContactEmailLengthRule(var maxLength: Int) : ScheduledLockerRuleRule {
    override fun check(item: ScheduledLockerRule): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerRuleVisibleGuardRule(var limit: Int) : ScheduledLockerRuleRule {
    override fun check(item: ScheduledLockerRule): MutableList<String> {
        if (item.visible && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerRuleTimezoneLengthRule(var maxLength: Int) : ScheduledLockerRuleRule {
    override fun check(item: ScheduledLockerRule): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerRuleCodeLengthRule(var maxLength: Int) : ScheduledLockerRuleRule {
    override fun check(item: ScheduledLockerRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerRuleRequiredTimezoneRule(var message: String) : ScheduledLockerRuleRule {
    override fun check(item: ScheduledLockerRule): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled locker rule. */
data class ScheduledLockerRuleValidator(var rules: MutableList<ScheduledLockerRuleRule>) {
    fun validate(item: ScheduledLockerRule): MutableList<String> {
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

    fun requireValid(item: ScheduledLockerRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLockerRule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLockerRuleValidator(): ScheduledLockerRuleValidator {
    return ScheduledLockerRuleValidator(
        mutableListOf<ScheduledLockerRuleRule>(
            ScheduledLockerRuleContactEmailLengthRule(120),
            ScheduledLockerRuleVisibleGuardRule(3600),
            ScheduledLockerRuleTimezoneLengthRule(64),
            ScheduledLockerRuleCodeLengthRule(64),
            ScheduledLockerRuleRequiredTimezoneRule("scheduled locker rule needs this field"),
        ),
    )
}
