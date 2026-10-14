package com.acme.transport.linehaul.backhaul.audit

/** A rule a backhaul audit must pass. Check returns its problems, or none. */
interface BackhaulAuditRule {
    fun check(item: BackhaulAudit): MutableList<String>
}

data class BackhaulAuditRequiredCodeRule(var message: String) : BackhaulAuditRule {
    override fun check(item: BackhaulAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAuditTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : BackhaulAuditRule {
    override fun check(item: BackhaulAudit): MutableList<String> {
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

data class BackhaulAuditMarginRangeRule(var min: Double, var max: Double) : BackhaulAuditRule {
    override fun check(item: BackhaulAudit): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class BackhaulAuditFuelLitersRangeRule(var min: Double, var max: Double) : BackhaulAuditRule {
    override fun check(item: BackhaulAudit): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAuditRequiredLabelRule(var message: String) : BackhaulAuditRule {
    override fun check(item: BackhaulAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul audit. */
data class BackhaulAuditValidator(var rules: MutableList<BackhaulAuditRule>) {
    fun validate(item: BackhaulAudit): MutableList<String> {
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

    fun requireValid(item: BackhaulAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulAuditValidator(): BackhaulAuditValidator {
    return BackhaulAuditValidator(
        mutableListOf<BackhaulAuditRule>(
            BackhaulAuditRequiredCodeRule("backhaul audit needs an entry"),
            BackhaulAuditTagsLimitRule(120, mutableListOf<String>("capped")),
            BackhaulAuditMarginRangeRule(6.1, 1862.6),
            BackhaulAuditFuelLitersRangeRule(2.6, 1530.5),
            BackhaulAuditRequiredLabelRule("backhaul audit needs a value"),
        ),
    )
}
