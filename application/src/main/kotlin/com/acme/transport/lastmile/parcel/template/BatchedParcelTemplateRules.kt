package com.acme.transport.lastmile.parcel.template

/** A rule a batched parcel template must pass. Check returns its problems, or none. */
interface BatchedParcelTemplateRule {
    fun check(item: BatchedParcelTemplate): MutableList<String>
}

data class BatchedParcelTemplateCodeLengthRule(var maxLength: Int) : BatchedParcelTemplateRule {
    override fun check(item: BatchedParcelTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelTemplateEtaMinutesRangeRule(var min: Int, var max: Int) : BatchedParcelTemplateRule {
    override fun check(item: BatchedParcelTemplate): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class BatchedParcelTemplateCodePrefixRule(var prefix: String) : BatchedParcelTemplateRule {
    override fun check(item: BatchedParcelTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelTemplateUpdatedAtRangeRule(var min: Int, var max: Int) : BatchedParcelTemplateRule {
    override fun check(item: BatchedParcelTemplate): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class BatchedParcelTemplateRouteCodeLengthRule(var maxLength: Int) : BatchedParcelTemplateRule {
    override fun check(item: BatchedParcelTemplate): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched parcel template. */
data class BatchedParcelTemplateValidator(var rules: MutableList<BatchedParcelTemplateRule>) {
    fun validate(item: BatchedParcelTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedParcelTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedParcelTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedParcelTemplateValidator(): BatchedParcelTemplateValidator {
    return BatchedParcelTemplateValidator(
        mutableListOf<BatchedParcelTemplateRule>(
            BatchedParcelTemplateCodeLengthRule(64),
            BatchedParcelTemplateEtaMinutesRangeRule(3600, 86400),
            BatchedParcelTemplateCodePrefixRule("Q"),
            BatchedParcelTemplateUpdatedAtRangeRule(32, 80),
            BatchedParcelTemplateRouteCodeLengthRule(120),
        ),
    )
}
