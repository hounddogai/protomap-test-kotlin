package com.acme.transport.freightpay.audit.auditproposal

/** A rule a rolling audit slot must pass. Check returns its problems, or none. */
interface RollingAuditSlotRule {
    fun check(item: RollingAuditSlot): MutableList<String>
}

data class RollingAuditSlotTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingAuditSlotRule {
    override fun check(item: RollingAuditSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RollingAuditSlotNameLengthRule(var maxLength: Int) : RollingAuditSlotRule {
    override fun check(item: RollingAuditSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditSlotCodeLengthRule(var maxLength: Int) : RollingAuditSlotRule {
    override fun check(item: RollingAuditSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditSlotCodePrefixRule(var prefix: String) : RollingAuditSlotRule {
    override fun check(item: RollingAuditSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling audit slot. */
data class RollingAuditSlotValidator(var rules: MutableList<RollingAuditSlotRule>) {
    fun validate(item: RollingAuditSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAuditSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAuditSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAuditSlotValidator(): RollingAuditSlotValidator {
    return RollingAuditSlotValidator(
        mutableListOf<RollingAuditSlotRule>(
            RollingAuditSlotTagsLimitRule(120, mutableListOf<String>("global", "pending")),
            RollingAuditSlotNameLengthRule(32),
            RollingAuditSlotCodeLengthRule(1000),
            RollingAuditSlotCodePrefixRule("OPS"),
        ),
    )
}
