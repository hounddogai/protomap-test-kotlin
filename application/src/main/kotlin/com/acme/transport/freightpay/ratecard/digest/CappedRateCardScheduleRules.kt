package com.acme.transport.freightpay.ratecard.digest

/** A rule a capped rate card schedule must pass. Check returns its problems, or none. */
interface CappedRateCardScheduleRule {
    fun check(item: CappedRateCardSchedule): MutableList<String>
}

data class CappedRateCardScheduleVisibleGuardRule(var limit: Int) : CappedRateCardScheduleRule {
    override fun check(item: CappedRateCardSchedule): MutableList<String> {
        if (item.visible && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardScheduleRequiredOwnerIdRule(var message: String) : CappedRateCardScheduleRule {
    override fun check(item: CappedRateCardSchedule): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardScheduleCodeLengthRule(var maxLength: Int) : CappedRateCardScheduleRule {
    override fun check(item: CappedRateCardSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardScheduleRequiredDockRule(var message: String) : CappedRateCardScheduleRule {
    override fun check(item: CappedRateCardSchedule): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped rate card schedule. */
data class CappedRateCardScheduleValidator(var rules: MutableList<CappedRateCardScheduleRule>) {
    fun validate(item: CappedRateCardSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRateCardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRateCardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRateCardScheduleValidator(): CappedRateCardScheduleValidator {
    return CappedRateCardScheduleValidator(
        mutableListOf<CappedRateCardScheduleRule>(
            CappedRateCardScheduleVisibleGuardRule(32),
            CappedRateCardScheduleRequiredOwnerIdRule("capped rate card schedule needs a value"),
            CappedRateCardScheduleCodeLengthRule(120),
            CappedRateCardScheduleRequiredDockRule("capped rate card schedule needs an entry"),
        ),
    )
}
