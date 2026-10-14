package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.text.normalize

/** A rule a batched qualification bundle must pass. Check returns its problems, or none. */
interface BatchedQualificationBundleRule {
    fun check(item: BatchedQualificationBundle): MutableList<String>
}

data class BatchedQualificationBundleCodePrefixRule(var prefix: String) : BatchedQualificationBundleRule {
    override fun check(item: BatchedQualificationBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationBundleRequiredDescriptionRule(
    var message: String,
) : BatchedQualificationBundleRule {
    override fun check(item: BatchedQualificationBundle): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationBundleRequiredLabelRule(var message: String) : BatchedQualificationBundleRule {
    override fun check(item: BatchedQualificationBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationBundleRouteCodeLengthRule(var maxLength: Int) : BatchedQualificationBundleRule {
    override fun check(item: BatchedQualificationBundle): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationBundleRequiredRouteCodeRule(
    var message: String,
) : BatchedQualificationBundleRule {
    override fun check(item: BatchedQualificationBundle): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationBundleAllowedRegionRule(
    var allowed: MutableList<String>,
) : BatchedQualificationBundleRule {
    override fun check(item: BatchedQualificationBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched qualification bundle. */
data class BatchedQualificationBundleValidator(var rules: MutableList<BatchedQualificationBundleRule>) {
    fun validate(item: BatchedQualificationBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedQualificationBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedQualificationBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedQualificationBundleValidator(): BatchedQualificationBundleValidator {
    return BatchedQualificationBundleValidator(
        mutableListOf<BatchedQualificationBundleRule>(
            BatchedQualificationBundleCodePrefixRule("OPS"),
            BatchedQualificationBundleRequiredDescriptionRule("batched qualification bundle needs this field"),
            BatchedQualificationBundleRequiredLabelRule("batched qualification bundle needs this field"),
            BatchedQualificationBundleRouteCodeLengthRule(1000),
            BatchedQualificationBundleRequiredRouteCodeRule("batched qualification bundle needs this field"),
            BatchedQualificationBundleAllowedRegionRule(mutableListOf<String>("scheduled")),
        ),
    )
}
