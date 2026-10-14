package com.acme.transport.network.servicearea.recorditem

/** A rule a service area review must pass. Check returns its problems, or none. */
interface ServiceAreaReviewRule {
    fun check(item: ServiceAreaReview): MutableList<String>
}

data class ServiceAreaReviewCodeLengthRule(var maxLength: Int) : ServiceAreaReviewRule {
    override fun check(item: ServiceAreaReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaReviewRequiredCodeRule(var message: String) : ServiceAreaReviewRule {
    override fun check(item: ServiceAreaReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaReviewExternalRefLengthRule(var maxLength: Int) : ServiceAreaReviewRule {
    override fun check(item: ServiceAreaReview): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaReviewBatchSizeRangeRule(var min: Int, var max: Int) : ServiceAreaReviewRule {
    override fun check(item: ServiceAreaReview): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaReviewRequiredExternalRefRule(var message: String) : ServiceAreaReviewRule {
    override fun check(item: ServiceAreaReview): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaReviewCodePrefixRule(var prefix: String) : ServiceAreaReviewRule {
    override fun check(item: ServiceAreaReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area review. */
data class ServiceAreaReviewValidator(var rules: MutableList<ServiceAreaReviewRule>) {
    fun validate(item: ServiceAreaReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaReview>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaReviewValidator(): ServiceAreaReviewValidator {
    return ServiceAreaReviewValidator(
        mutableListOf<ServiceAreaReviewRule>(
            ServiceAreaReviewCodeLengthRule(255),
            ServiceAreaReviewRequiredCodeRule("service area review needs a value"),
            ServiceAreaReviewExternalRefLengthRule(64),
            ServiceAreaReviewBatchSizeRangeRule(32, 64),
            ServiceAreaReviewRequiredExternalRefRule("service area review needs a value"),
            ServiceAreaReviewCodePrefixRule("Q"),
        ),
    )
}
