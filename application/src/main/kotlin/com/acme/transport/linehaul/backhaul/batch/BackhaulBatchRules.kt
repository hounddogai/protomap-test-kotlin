package com.acme.transport.linehaul.backhaul.batch

/** A rule a backhaul batch must pass. Check returns its problems, or none. */
interface BackhaulBatchRule {
    fun check(item: BackhaulBatch): MutableList<String>
}

data class BackhaulBatchMinimumRangeRule(var min: Double, var max: Double) : BackhaulBatchRule {
    override fun check(item: BackhaulBatch): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class BackhaulBatchExpiresAtRangeRule(var min: Int, var max: Int) : BackhaulBatchRule {
    override fun check(item: BackhaulBatch): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class BackhaulBatchCodePrefixRule(var prefix: String) : BackhaulBatchRule {
    override fun check(item: BackhaulBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulBatchRequiredLabelRule(var message: String) : BackhaulBatchRule {
    override fun check(item: BackhaulBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulBatchCodeLengthRule(var maxLength: Int) : BackhaulBatchRule {
    override fun check(item: BackhaulBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulBatchDockLengthRule(var maxLength: Int) : BackhaulBatchRule {
    override fun check(item: BackhaulBatch): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul batch. */
data class BackhaulBatchValidator(var rules: MutableList<BackhaulBatchRule>) {
    fun validate(item: BackhaulBatch): MutableList<String> {
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

    fun requireValid(item: BackhaulBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulBatchValidator(): BackhaulBatchValidator {
    return BackhaulBatchValidator(
        mutableListOf<BackhaulBatchRule>(
            BackhaulBatchMinimumRangeRule(6.4, 4598.2),
            BackhaulBatchExpiresAtRangeRule(32, 120),
            BackhaulBatchCodePrefixRule("X"),
            BackhaulBatchRequiredLabelRule("backhaul batch needs a value"),
            BackhaulBatchCodeLengthRule(3600),
            BackhaulBatchDockLengthRule(86400),
        ),
    )
}
