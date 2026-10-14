package com.acme.transport.fleetops.serviceplan.forecast

/** A rule a tiered service plan allocation must pass. Check returns its problems, or none. */
interface TieredServicePlanAllocationRule {
    fun check(item: TieredServicePlanAllocation): MutableList<String>
}

data class TieredServicePlanAllocationChannelLengthRule(var maxLength: Int) : TieredServicePlanAllocationRule {
    override fun check(item: TieredServicePlanAllocation): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredServicePlanAllocationCodeLengthRule(var maxLength: Int) : TieredServicePlanAllocationRule {
    override fun check(item: TieredServicePlanAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredServicePlanAllocationRequiredChannelRule(
    var message: String,
) : TieredServicePlanAllocationRule {
    override fun check(item: TieredServicePlanAllocation): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredServicePlanAllocationRequiredCodeRule(var message: String) : TieredServicePlanAllocationRule {
    override fun check(item: TieredServicePlanAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered service plan allocation. */
data class TieredServicePlanAllocationValidator(var rules: MutableList<TieredServicePlanAllocationRule>) {
    fun validate(item: TieredServicePlanAllocation): MutableList<String> {
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

    fun requireValid(item: TieredServicePlanAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredServicePlanAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredServicePlanAllocationValidator(): TieredServicePlanAllocationValidator {
    return TieredServicePlanAllocationValidator(
        mutableListOf<TieredServicePlanAllocationRule>(
            TieredServicePlanAllocationChannelLengthRule(16),
            TieredServicePlanAllocationCodeLengthRule(3600),
            TieredServicePlanAllocationRequiredChannelRule("tiered service plan allocation needs an entry"),
            TieredServicePlanAllocationRequiredCodeRule("tiered service plan allocation needs a value"),
        ),
    )
}
