package com.acme.transport.lastmile.drop.dropnotice

/** A rule a rolling drop notice must pass. Check returns its problems, or none. */
interface RollingDropNoticeRule {
    fun check(item: RollingDropNotice): MutableList<String>
}

data class RollingDropNoticeRequiredCodeRule(var message: String) : RollingDropNoticeRule {
    override fun check(item: RollingDropNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDropNoticeCodeLengthRule(var maxLength: Int) : RollingDropNoticeRule {
    override fun check(item: RollingDropNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDropNoticeCodePrefixRule(var prefix: String) : RollingDropNoticeRule {
    override fun check(item: RollingDropNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDropNoticeRequiredTimezoneRule(var message: String) : RollingDropNoticeRule {
    override fun check(item: RollingDropNotice): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDropNoticeRequiredLabelRule(var message: String) : RollingDropNoticeRule {
    override fun check(item: RollingDropNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling drop notice. */
data class RollingDropNoticeValidator(var rules: MutableList<RollingDropNoticeRule>) {
    fun validate(item: RollingDropNotice): MutableList<String> {
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

    fun requireValid(item: RollingDropNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDropNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDropNoticeValidator(): RollingDropNoticeValidator {
    return RollingDropNoticeValidator(
        mutableListOf<RollingDropNoticeRule>(
            RollingDropNoticeRequiredCodeRule("rolling drop notice needs an entry"),
            RollingDropNoticeCodeLengthRule(1000),
            RollingDropNoticeCodePrefixRule("X"),
            RollingDropNoticeRequiredTimezoneRule("rolling drop notice needs an entry"),
            RollingDropNoticeRequiredLabelRule("rolling drop notice needs an entry"),
        ),
    )
}
