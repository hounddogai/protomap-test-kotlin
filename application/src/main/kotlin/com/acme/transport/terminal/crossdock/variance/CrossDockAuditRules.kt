package com.acme.transport.terminal.crossdock.variance

/** A rule a cross dock audit must pass. Check returns its problems, or none. */
interface CrossDockAuditRule {
    fun check(item: CrossDockAudit): MutableList<String>
}

data class CrossDockAuditCodeLengthRule(var maxLength: Int) : CrossDockAuditRule {
    override fun check(item: CrossDockAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAuditTrailerIdLengthRule(var maxLength: Int) : CrossDockAuditRule {
    override fun check(item: CrossDockAudit): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAuditRequiredRegionRule(var message: String) : CrossDockAuditRule {
    override fun check(item: CrossDockAudit): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAuditPriorityRangeRule(var min: Int, var max: Int) : CrossDockAuditRule {
    override fun check(item: CrossDockAudit): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class CrossDockAuditRequiredNameRule(var message: String) : CrossDockAuditRule {
    override fun check(item: CrossDockAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock audit. */
data class CrossDockAuditValidator(var rules: MutableList<CrossDockAuditRule>) {
    fun validate(item: CrossDockAudit): MutableList<String> {
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

    fun requireValid(item: CrossDockAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockAuditValidator(): CrossDockAuditValidator {
    return CrossDockAuditValidator(
        mutableListOf<CrossDockAuditRule>(
            CrossDockAuditCodeLengthRule(3600),
            CrossDockAuditTrailerIdLengthRule(255),
            CrossDockAuditRequiredRegionRule("cross dock audit needs this field"),
            CrossDockAuditPriorityRangeRule(16, 64),
            CrossDockAuditRequiredNameRule("cross dock audit needs an entry"),
        ),
    )
}
