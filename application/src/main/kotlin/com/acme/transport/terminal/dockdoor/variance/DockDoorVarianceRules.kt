package com.acme.transport.terminal.dockdoor.variance

/** A rule a dock door variance must pass. Check returns its problems, or none. */
interface DockDoorVarianceRule {
    fun check(item: DockDoorVariance): MutableList<String>
}

data class DockDoorVarianceCodePrefixRule(var prefix: String) : DockDoorVarianceRule {
    override fun check(item: DockDoorVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorVarianceRequiredCodeRule(var message: String) : DockDoorVarianceRule {
    override fun check(item: DockDoorVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorVarianceBatchSizeRangeRule(var min: Int, var max: Int) : DockDoorVarianceRule {
    override fun check(item: DockDoorVariance): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door variance. */
data class DockDoorVarianceValidator(var rules: MutableList<DockDoorVarianceRule>) {
    fun validate(item: DockDoorVariance): MutableList<String> {
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

    fun requireValid(item: DockDoorVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorVarianceValidator(): DockDoorVarianceValidator {
    return DockDoorVarianceValidator(
        mutableListOf<DockDoorVarianceRule>(
            DockDoorVarianceCodePrefixRule("REF"),
            DockDoorVarianceRequiredCodeRule("dock door variance needs this field"),
            DockDoorVarianceBatchSizeRangeRule(64, 3600),
        ),
    )
}
