package com.acme.transport.terminal.sortation.sortationtier

/** A rule a sortation audit must pass. Check returns its problems, or none. */
interface SortationAuditRule {
    fun check(item: SortationAudit): MutableList<String>
}

data class SortationAuditRequiredCodeRule(var message: String) : SortationAuditRule {
    override fun check(item: SortationAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationAuditRequiredNameRule(var message: String) : SortationAuditRule {
    override fun check(item: SortationAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationAuditCodePrefixRule(var prefix: String) : SortationAuditRule {
    override fun check(item: SortationAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationAuditDepotLengthRule(var maxLength: Int) : SortationAuditRule {
    override fun check(item: SortationAudit): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation audit. */
data class SortationAuditValidator(var rules: MutableList<SortationAuditRule>) {
    fun validate(item: SortationAudit): MutableList<String> {
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

    fun requireValid(item: SortationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationAuditValidator(): SortationAuditValidator {
    return SortationAuditValidator(
        mutableListOf<SortationAuditRule>(
            SortationAuditRequiredCodeRule("sortation audit needs a value"),
            SortationAuditRequiredNameRule("sortation audit needs a value"),
            SortationAuditCodePrefixRule("ACM"),
            SortationAuditDepotLengthRule(16),
        ),
    )
}
