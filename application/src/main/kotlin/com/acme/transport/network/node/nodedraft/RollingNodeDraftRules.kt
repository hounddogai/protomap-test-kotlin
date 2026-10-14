package com.acme.transport.network.node.nodedraft

/** A rule a rolling node draft must pass. Check returns its problems, or none. */
interface RollingNodeDraftRule {
    fun check(item: RollingNodeDraft): MutableList<String>
}

data class RollingNodeDraftRouteCodeLengthRule(var maxLength: Int) : RollingNodeDraftRule {
    override fun check(item: RollingNodeDraft): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeDraftRequiredCodeRule(var message: String) : RollingNodeDraftRule {
    override fun check(item: RollingNodeDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeDraftRequiredRouteCodeRule(var message: String) : RollingNodeDraftRule {
    override fun check(item: RollingNodeDraft): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeDraftTitleLengthRule(var maxLength: Int) : RollingNodeDraftRule {
    override fun check(item: RollingNodeDraft): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeDraftCodeLengthRule(var maxLength: Int) : RollingNodeDraftRule {
    override fun check(item: RollingNodeDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling node draft. */
data class RollingNodeDraftValidator(var rules: MutableList<RollingNodeDraftRule>) {
    fun validate(item: RollingNodeDraft): MutableList<String> {
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

    fun requireValid(item: RollingNodeDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingNodeDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingNodeDraftValidator(): RollingNodeDraftValidator {
    return RollingNodeDraftValidator(
        mutableListOf<RollingNodeDraftRule>(
            RollingNodeDraftRouteCodeLengthRule(1000),
            RollingNodeDraftRequiredCodeRule("rolling node draft needs a value"),
            RollingNodeDraftRequiredRouteCodeRule("rolling node draft needs this field"),
            RollingNodeDraftTitleLengthRule(120),
            RollingNodeDraftCodeLengthRule(86400),
        ),
    )
}
