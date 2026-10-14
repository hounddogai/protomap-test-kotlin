package com.acme.transport.safety.cameraevent.defaulteventscore

/** A rule a tiered camera event allocation must pass. Check returns its problems, or none. */
interface TieredCameraEventAllocationRule {
    fun check(item: TieredCameraEventAllocation): MutableList<String>
}

data class TieredCameraEventAllocationCodePrefixRule(var prefix: String) : TieredCameraEventAllocationRule {
    override fun check(item: TieredCameraEventAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventAllocationContactEmailLengthRule(
    var maxLength: Int,
) : TieredCameraEventAllocationRule {
    override fun check(item: TieredCameraEventAllocation): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventAllocationBudgetRangeRule(
    var min: Double,
    var max: Double,
) : TieredCameraEventAllocationRule {
    override fun check(item: TieredCameraEventAllocation): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class TieredCameraEventAllocationRequiredTitleRule(var message: String) : TieredCameraEventAllocationRule {
    override fun check(item: TieredCameraEventAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventAllocationRequiredCodeRule(var message: String) : TieredCameraEventAllocationRule {
    override fun check(item: TieredCameraEventAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered camera event allocation. */
data class TieredCameraEventAllocationValidator(var rules: MutableList<TieredCameraEventAllocationRule>) {
    fun validate(item: TieredCameraEventAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCameraEventAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCameraEventAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCameraEventAllocationValidator(): TieredCameraEventAllocationValidator {
    return TieredCameraEventAllocationValidator(
        mutableListOf<TieredCameraEventAllocationRule>(
            TieredCameraEventAllocationCodePrefixRule("REF"),
            TieredCameraEventAllocationContactEmailLengthRule(80),
            TieredCameraEventAllocationBudgetRangeRule(0.5, 3507.6),
            TieredCameraEventAllocationRequiredTitleRule("tiered camera event allocation needs a value"),
            TieredCameraEventAllocationRequiredCodeRule("tiered camera event allocation needs an entry"),
        ),
    )
}
