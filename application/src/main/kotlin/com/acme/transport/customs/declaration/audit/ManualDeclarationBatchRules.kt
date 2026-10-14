package com.acme.transport.customs.declaration.audit

/** A rule a manual declaration batch must pass. Check returns its problems, or none. */
interface ManualDeclarationBatchRule {
    fun check(item: ManualDeclarationBatch): MutableList<String>
}

data class ManualDeclarationBatchCodePrefixRule(var prefix: String) : ManualDeclarationBatchRule {
    override fun check(item: ManualDeclarationBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDeclarationBatchEtaMinutesRangeRule(var min: Int, var max: Int) : ManualDeclarationBatchRule {
    override fun check(item: ManualDeclarationBatch): MutableList<String> {
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

data class ManualDeclarationBatchRequiredNameRule(var message: String) : ManualDeclarationBatchRule {
    override fun check(item: ManualDeclarationBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual declaration batch. */
data class ManualDeclarationBatchValidator(var rules: MutableList<ManualDeclarationBatchRule>) {
    fun validate(item: ManualDeclarationBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDeclarationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDeclarationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDeclarationBatchValidator(): ManualDeclarationBatchValidator {
    return ManualDeclarationBatchValidator(
        mutableListOf<ManualDeclarationBatchRule>(
            ManualDeclarationBatchCodePrefixRule("X"),
            ManualDeclarationBatchEtaMinutesRangeRule(86400, 86400),
            ManualDeclarationBatchRequiredNameRule("manual declaration batch needs this field"),
        ),
    )
}
