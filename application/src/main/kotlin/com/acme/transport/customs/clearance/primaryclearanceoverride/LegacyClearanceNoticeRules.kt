package com.acme.transport.customs.clearance.primaryclearanceoverride

/** A rule a legacy clearance notice must pass. Check returns its problems, or none. */
interface LegacyClearanceNoticeRule {
    fun check(item: LegacyClearanceNotice): MutableList<String>
}

data class LegacyClearanceNoticeCodeLengthRule(var maxLength: Int) : LegacyClearanceNoticeRule {
    override fun check(item: LegacyClearanceNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceNoticeCodePrefixRule(var prefix: String) : LegacyClearanceNoticeRule {
    override fun check(item: LegacyClearanceNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceNoticeRequiredCodeRule(var message: String) : LegacyClearanceNoticeRule {
    override fun check(item: LegacyClearanceNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy clearance notice. */
data class LegacyClearanceNoticeValidator(var rules: MutableList<LegacyClearanceNoticeRule>) {
    fun validate(item: LegacyClearanceNotice): MutableList<String> {
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

    fun requireValid(item: LegacyClearanceNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyClearanceNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyClearanceNoticeValidator(): LegacyClearanceNoticeValidator {
    return LegacyClearanceNoticeValidator(
        mutableListOf<LegacyClearanceNoticeRule>(
            LegacyClearanceNoticeCodeLengthRule(255),
            LegacyClearanceNoticeCodePrefixRule("ACM"),
            LegacyClearanceNoticeRequiredCodeRule("legacy clearance notice needs a value"),
        ),
    )
}
