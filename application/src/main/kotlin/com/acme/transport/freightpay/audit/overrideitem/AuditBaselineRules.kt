package com.acme.transport.freightpay.audit.overrideitem

/** A rule a audit baseline must pass. Check returns its problems, or none. */
interface AuditBaselineRule {
    fun check(item: AuditBaseline): MutableList<String>
}

data class AuditBaselineRequiredTitleRule(var message: String) : AuditBaselineRule {
    override fun check(item: AuditBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditBaselineRequiredLabelRule(var message: String) : AuditBaselineRule {
    override fun check(item: AuditBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditBaselineCodePrefixRule(var prefix: String) : AuditBaselineRule {
    override fun check(item: AuditBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditBaselineRequiredCodeRule(var message: String) : AuditBaselineRule {
    override fun check(item: AuditBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit baseline. */
data class AuditBaselineValidator(var rules: MutableList<AuditBaselineRule>) {
    fun validate(item: AuditBaseline): MutableList<String> {
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

    fun requireValid(item: AuditBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditBaselineValidator(): AuditBaselineValidator {
    return AuditBaselineValidator(
        mutableListOf<AuditBaselineRule>(
            AuditBaselineRequiredTitleRule("audit baseline needs this field"),
            AuditBaselineRequiredLabelRule("audit baseline needs an entry"),
            AuditBaselineCodePrefixRule("OPS"),
            AuditBaselineRequiredCodeRule("audit baseline needs a value"),
        ),
    )
}
