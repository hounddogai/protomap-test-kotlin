package com.acme.transport.customs.clearance.clearanceoverride

/** A rule a clearance notice must pass. Check returns its problems, or none. */
interface ClearanceNoticeRule {
    fun check(item: ClearanceNotice): MutableList<String>
}

data class ClearanceNoticeDockLengthRule(var maxLength: Int) : ClearanceNoticeRule {
    override fun check(item: ClearanceNotice): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceNoticeRouteCodeLengthRule(var maxLength: Int) : ClearanceNoticeRule {
    override fun check(item: ClearanceNotice): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceNoticeRequiredCodeRule(var message: String) : ClearanceNoticeRule {
    override fun check(item: ClearanceNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceNoticeCodePrefixRule(var prefix: String) : ClearanceNoticeRule {
    override fun check(item: ClearanceNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance notice. */
data class ClearanceNoticeValidator(var rules: MutableList<ClearanceNoticeRule>) {
    fun validate(item: ClearanceNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceNoticeValidator(): ClearanceNoticeValidator {
    return ClearanceNoticeValidator(
        mutableListOf<ClearanceNoticeRule>(
            ClearanceNoticeDockLengthRule(64),
            ClearanceNoticeRouteCodeLengthRule(80),
            ClearanceNoticeRequiredCodeRule("clearance notice needs an entry"),
            ClearanceNoticeCodePrefixRule("REF"),
        ),
    )
}
