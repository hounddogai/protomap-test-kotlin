package com.acme.transport.terminal.dockdoor.doorclaim

/** A rule a dock door batch must pass. Check returns its problems, or none. */
interface DockDoorBatchRule {
    fun check(item: DockDoorBatch): MutableList<String>
}

data class DockDoorBatchQuantityRangeRule(var min: Int, var max: Int) : DockDoorBatchRule {
    override fun check(item: DockDoorBatch): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class DockDoorBatchRequiredCodeRule(var message: String) : DockDoorBatchRule {
    override fun check(item: DockDoorBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorBatchRatioRangeRule(var min: Double, var max: Double) : DockDoorBatchRule {
    override fun check(item: DockDoorBatch): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class DockDoorBatchCodePrefixRule(var prefix: String) : DockDoorBatchRule {
    override fun check(item: DockDoorBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorBatchTitleLengthRule(var maxLength: Int) : DockDoorBatchRule {
    override fun check(item: DockDoorBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door batch. */
data class DockDoorBatchValidator(var rules: MutableList<DockDoorBatchRule>) {
    fun validate(item: DockDoorBatch): MutableList<String> {
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

    fun requireValid(item: DockDoorBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorBatchValidator(): DockDoorBatchValidator {
    return DockDoorBatchValidator(
        mutableListOf<DockDoorBatchRule>(
            DockDoorBatchQuantityRangeRule(16, 255),
            DockDoorBatchRequiredCodeRule("dock door batch needs a value"),
            DockDoorBatchRatioRangeRule(4.0, 4036.2),
            DockDoorBatchCodePrefixRule("ACM"),
            DockDoorBatchTitleLengthRule(1000),
        ),
    )
}
