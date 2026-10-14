package com.acme.transport.lastmile.redelivery.ledger

/** A rule a lenient redelivery audit must pass. Check returns its problems, or none. */
interface LenientRedeliveryAuditRule {
    fun check(item: LenientRedeliveryAudit): MutableList<String>
}

data class LenientRedeliveryAuditTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LenientRedeliveryAuditRule {
    override fun check(item: LenientRedeliveryAudit): MutableList<String> {
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

data class LenientRedeliveryAuditCeilingRangeRule(
    var min: Double,
    var max: Double,
) : LenientRedeliveryAuditRule {
    override fun check(item: LenientRedeliveryAudit): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class LenientRedeliveryAuditOdometerRangeRule(var min: Int, var max: Int) : LenientRedeliveryAuditRule {
    override fun check(item: LenientRedeliveryAudit): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class LenientRedeliveryAuditCodeLengthRule(var maxLength: Int) : LenientRedeliveryAuditRule {
    override fun check(item: LenientRedeliveryAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient redelivery audit. */
data class LenientRedeliveryAuditValidator(var rules: MutableList<LenientRedeliveryAuditRule>) {
    fun validate(item: LenientRedeliveryAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRedeliveryAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRedeliveryAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRedeliveryAuditValidator(): LenientRedeliveryAuditValidator {
    return LenientRedeliveryAuditValidator(
        mutableListOf<LenientRedeliveryAuditRule>(
            LenientRedeliveryAuditTagsLimitRule(1000, mutableListOf<String>()),
            LenientRedeliveryAuditCeilingRangeRule(8.7, 4745.1),
            LenientRedeliveryAuditOdometerRangeRule(32, 120),
            LenientRedeliveryAuditCodeLengthRule(3600),
        ),
    )
}
