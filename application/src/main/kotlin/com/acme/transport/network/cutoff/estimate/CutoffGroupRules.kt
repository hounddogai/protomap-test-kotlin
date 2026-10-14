package com.acme.transport.network.cutoff.estimate

/** A rule a cutoff group must pass. Check returns its problems, or none. */
interface CutoffGroupRule {
    fun check(item: CutoffGroup): MutableList<String>
}

data class CutoffGroupBatchSizeRangeRule(var min: Int, var max: Int) : CutoffGroupRule {
    override fun check(item: CutoffGroup): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffGroupRequiredLabelRule(var message: String) : CutoffGroupRule {
    override fun check(item: CutoffGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffGroupPriorityRangeRule(var min: Int, var max: Int) : CutoffGroupRule {
    override fun check(item: CutoffGroup): MutableList<String> {
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

data class CutoffGroupRequiredNameRule(var message: String) : CutoffGroupRule {
    override fun check(item: CutoffGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffGroupRateRangeRule(var min: Double, var max: Double) : CutoffGroupRule {
    override fun check(item: CutoffGroup): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class CutoffGroupRequiredCodeRule(var message: String) : CutoffGroupRule {
    override fun check(item: CutoffGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff group. */
data class CutoffGroupValidator(var rules: MutableList<CutoffGroupRule>) {
    fun validate(item: CutoffGroup): MutableList<String> {
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

    fun requireValid(item: CutoffGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffGroupValidator(): CutoffGroupValidator {
    return CutoffGroupValidator(
        mutableListOf<CutoffGroupRule>(
            CutoffGroupBatchSizeRangeRule(64, 3600),
            CutoffGroupRequiredLabelRule("cutoff group needs this field"),
            CutoffGroupPriorityRangeRule(120, 3600),
            CutoffGroupRequiredNameRule("cutoff group needs this field"),
            CutoffGroupRateRangeRule(9.4, 3113.7),
            CutoffGroupRequiredCodeRule("cutoff group needs an entry"),
        ),
    )
}
