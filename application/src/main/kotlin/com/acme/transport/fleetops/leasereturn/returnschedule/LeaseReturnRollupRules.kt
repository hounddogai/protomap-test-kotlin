package com.acme.transport.fleetops.leasereturn.returnschedule

/** A rule a lease return rollup must pass. Check returns its problems, or none. */
interface LeaseReturnRollupRule {
    fun check(item: LeaseReturnRollup): MutableList<String>
}

data class LeaseReturnRollupCodePrefixRule(var prefix: String) : LeaseReturnRollupRule {
    override fun check(item: LeaseReturnRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRollupBatchSizeRangeRule(var min: Int, var max: Int) : LeaseReturnRollupRule {
    override fun check(item: LeaseReturnRollup): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRollupRequiredNameRule(var message: String) : LeaseReturnRollupRule {
    override fun check(item: LeaseReturnRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return rollup. */
data class LeaseReturnRollupValidator(var rules: MutableList<LeaseReturnRollupRule>) {
    fun validate(item: LeaseReturnRollup): MutableList<String> {
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

    fun requireValid(item: LeaseReturnRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnRollupValidator(): LeaseReturnRollupValidator {
    return LeaseReturnRollupValidator(
        mutableListOf<LeaseReturnRollupRule>(
            LeaseReturnRollupCodePrefixRule("Q"),
            LeaseReturnRollupBatchSizeRangeRule(64, 255),
            LeaseReturnRollupRequiredNameRule("lease return rollup needs this field"),
        ),
    )
}
