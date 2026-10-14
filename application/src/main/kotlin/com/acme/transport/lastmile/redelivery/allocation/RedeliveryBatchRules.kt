package com.acme.transport.lastmile.redelivery.allocation

/** A rule a redelivery batch must pass. Check returns its problems, or none. */
interface RedeliveryBatchRule {
    fun check(item: RedeliveryBatch): MutableList<String>
}

data class RedeliveryBatchCodeLengthRule(var maxLength: Int) : RedeliveryBatchRule {
    override fun check(item: RedeliveryBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryBatchRequiredRouteCodeRule(var message: String) : RedeliveryBatchRule {
    override fun check(item: RedeliveryBatch): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryBatchCodePrefixRule(var prefix: String) : RedeliveryBatchRule {
    override fun check(item: RedeliveryBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryBatchRequiredCodeRule(var message: String) : RedeliveryBatchRule {
    override fun check(item: RedeliveryBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery batch. */
data class RedeliveryBatchValidator(var rules: MutableList<RedeliveryBatchRule>) {
    fun validate(item: RedeliveryBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryBatchValidator(): RedeliveryBatchValidator {
    return RedeliveryBatchValidator(
        mutableListOf<RedeliveryBatchRule>(
            RedeliveryBatchCodeLengthRule(1000),
            RedeliveryBatchRequiredRouteCodeRule("redelivery batch needs an entry"),
            RedeliveryBatchCodePrefixRule("REF"),
            RedeliveryBatchRequiredCodeRule("redelivery batch needs an entry"),
        ),
    )
}
