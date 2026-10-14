package com.acme.transport.freightpay.accessorial.accessorialentry

/** A rule a accessorial slot must pass. Check returns its problems, or none. */
interface AccessorialSlotRule {
    fun check(item: AccessorialSlot): MutableList<String>
}

data class AccessorialSlotRequiredSummaryRule(var message: String) : AccessorialSlotRule {
    override fun check(item: AccessorialSlot): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialSlotRequiredReviewerRule(var message: String) : AccessorialSlotRule {
    override fun check(item: AccessorialSlot): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialSlotRequiredTitleRule(var message: String) : AccessorialSlotRule {
    override fun check(item: AccessorialSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialSlotSummaryLengthRule(var maxLength: Int) : AccessorialSlotRule {
    override fun check(item: AccessorialSlot): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialSlotReviewerLengthRule(var maxLength: Int) : AccessorialSlotRule {
    override fun check(item: AccessorialSlot): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial slot. */
data class AccessorialSlotValidator(var rules: MutableList<AccessorialSlotRule>) {
    fun validate(item: AccessorialSlot): MutableList<String> {
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

    fun requireValid(item: AccessorialSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialSlotValidator(): AccessorialSlotValidator {
    return AccessorialSlotValidator(
        mutableListOf<AccessorialSlotRule>(
            AccessorialSlotRequiredSummaryRule("accessorial slot needs an entry"),
            AccessorialSlotRequiredReviewerRule("accessorial slot needs this field"),
            AccessorialSlotRequiredTitleRule("accessorial slot needs this field"),
            AccessorialSlotSummaryLengthRule(255),
            AccessorialSlotReviewerLengthRule(86400),
        ),
    )
}
