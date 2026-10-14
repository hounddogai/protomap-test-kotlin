package com.acme.transport.network.transittime.timedigest

/** A rule a lenient transit time audit must pass. Check returns its problems, or none. */
interface LenientTransitTimeAuditRule {
    fun check(item: LenientTransitTimeAudit): MutableList<String>
}

data class LenientTransitTimeAuditQuantityRangeRule(var min: Int, var max: Int) : LenientTransitTimeAuditRule {
    override fun check(item: LenientTransitTimeAudit): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeAuditCategoryLengthRule(var maxLength: Int) : LenientTransitTimeAuditRule {
    override fun check(item: LenientTransitTimeAudit): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeAuditCodePrefixRule(var prefix: String) : LenientTransitTimeAuditRule {
    override fun check(item: LenientTransitTimeAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeAuditRequiredDriverIdRule(var message: String) : LenientTransitTimeAuditRule {
    override fun check(item: LenientTransitTimeAudit): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeAuditDriverIdLengthRule(var maxLength: Int) : LenientTransitTimeAuditRule {
    override fun check(item: LenientTransitTimeAudit): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient transit time audit. */
data class LenientTransitTimeAuditValidator(var rules: MutableList<LenientTransitTimeAuditRule>) {
    fun validate(item: LenientTransitTimeAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientTransitTimeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTransitTimeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTransitTimeAuditValidator(): LenientTransitTimeAuditValidator {
    return LenientTransitTimeAuditValidator(
        mutableListOf<LenientTransitTimeAuditRule>(
            LenientTransitTimeAuditQuantityRangeRule(32, 3600),
            LenientTransitTimeAuditCategoryLengthRule(16),
            LenientTransitTimeAuditCodePrefixRule("ACM"),
            LenientTransitTimeAuditRequiredDriverIdRule("lenient transit time audit needs an entry"),
            LenientTransitTimeAuditDriverIdLengthRule(1000),
        ),
    )
}
