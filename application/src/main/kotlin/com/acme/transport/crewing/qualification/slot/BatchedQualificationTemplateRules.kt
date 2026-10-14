package com.acme.transport.crewing.qualification.slot

import com.acme.transport.kit.text.normalize

/** A rule a batched qualification template must pass. Check returns its problems, or none. */
interface BatchedQualificationTemplateRule {
    fun check(item: BatchedQualificationTemplate): MutableList<String>
}

data class BatchedQualificationTemplateRequiredCodeRule(
    var message: String,
) : BatchedQualificationTemplateRule {
    override fun check(item: BatchedQualificationTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationTemplateAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedQualificationTemplateRule {
    override fun check(item: BatchedQualificationTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationTemplateCeilingRangeRule(
    var min: Double,
    var max: Double,
) : BatchedQualificationTemplateRule {
    override fun check(item: BatchedQualificationTemplate): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationTemplateCodePrefixRule(var prefix: String) : BatchedQualificationTemplateRule {
    override fun check(item: BatchedQualificationTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationTemplateActiveGuardRule(var limit: Int) : BatchedQualificationTemplateRule {
    override fun check(item: BatchedQualificationTemplate): MutableList<String> {
        if (item.active && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched qualification template. */
data class BatchedQualificationTemplateValidator(var rules: MutableList<BatchedQualificationTemplateRule>) {
    fun validate(item: BatchedQualificationTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedQualificationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedQualificationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedQualificationTemplateValidator(): BatchedQualificationTemplateValidator {
    return BatchedQualificationTemplateValidator(
        mutableListOf<BatchedQualificationTemplateRule>(
            BatchedQualificationTemplateRequiredCodeRule("batched qualification template needs a value"),
            BatchedQualificationTemplateAllowedCodeRule(mutableListOf<String>("pending")),
            BatchedQualificationTemplateCeilingRangeRule(0.8, 4883.0),
            BatchedQualificationTemplateCodePrefixRule("ACM"),
            BatchedQualificationTemplateActiveGuardRule(64),
        ),
    )
}
