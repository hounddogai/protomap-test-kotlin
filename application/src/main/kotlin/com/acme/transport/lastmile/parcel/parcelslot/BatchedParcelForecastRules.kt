package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.text.normalize

/** A rule a batched parcel forecast must pass. Check returns its problems, or none. */
interface BatchedParcelForecastRule {
    fun check(item: BatchedParcelForecast): MutableList<String>
}

data class BatchedParcelForecastRequiredCodeRule(var message: String) : BatchedParcelForecastRule {
    override fun check(item: BatchedParcelForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelForecastCodePrefixRule(var prefix: String) : BatchedParcelForecastRule {
    override fun check(item: BatchedParcelForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelForecastChecksumLengthRule(var maxLength: Int) : BatchedParcelForecastRule {
    override fun check(item: BatchedParcelForecast): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelForecastRequiredTitleRule(var message: String) : BatchedParcelForecastRule {
    override fun check(item: BatchedParcelForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelForecastAllowedChecksumRule(
    var allowed: MutableList<String>,
) : BatchedParcelForecastRule {
    override fun check(item: BatchedParcelForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class BatchedParcelForecastTitleLengthRule(var maxLength: Int) : BatchedParcelForecastRule {
    override fun check(item: BatchedParcelForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched parcel forecast. */
data class BatchedParcelForecastValidator(var rules: MutableList<BatchedParcelForecastRule>) {
    fun validate(item: BatchedParcelForecast): MutableList<String> {
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

    fun requireValid(item: BatchedParcelForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedParcelForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedParcelForecastValidator(): BatchedParcelForecastValidator {
    return BatchedParcelForecastValidator(
        mutableListOf<BatchedParcelForecastRule>(
            BatchedParcelForecastRequiredCodeRule("batched parcel forecast needs this field"),
            BatchedParcelForecastCodePrefixRule("REF"),
            BatchedParcelForecastChecksumLengthRule(64),
            BatchedParcelForecastRequiredTitleRule("batched parcel forecast needs this field"),
            BatchedParcelForecastAllowedChecksumRule(mutableListOf<String>("adaptive")),
            BatchedParcelForecastTitleLengthRule(3600),
        ),
    )
}
