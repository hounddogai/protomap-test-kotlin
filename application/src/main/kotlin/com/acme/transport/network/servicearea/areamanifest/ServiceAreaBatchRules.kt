package com.acme.transport.network.servicearea.areamanifest

/** A rule a service area batch must pass. Check returns its problems, or none. */
interface ServiceAreaBatchRule {
    fun check(item: ServiceAreaBatch): MutableList<String>
}

data class ServiceAreaBatchRequiredNameRule(var message: String) : ServiceAreaBatchRule {
    override fun check(item: ServiceAreaBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBatchSummaryLengthRule(var maxLength: Int) : ServiceAreaBatchRule {
    override fun check(item: ServiceAreaBatch): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBatchRequiredNotesRule(var message: String) : ServiceAreaBatchRule {
    override fun check(item: ServiceAreaBatch): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBatchRequiredCodeRule(var message: String) : ServiceAreaBatchRule {
    override fun check(item: ServiceAreaBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaBatchCodeLengthRule(var maxLength: Int) : ServiceAreaBatchRule {
    override fun check(item: ServiceAreaBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area batch. */
data class ServiceAreaBatchValidator(var rules: MutableList<ServiceAreaBatchRule>) {
    fun validate(item: ServiceAreaBatch): MutableList<String> {
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

    fun requireValid(item: ServiceAreaBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaBatchValidator(): ServiceAreaBatchValidator {
    return ServiceAreaBatchValidator(
        mutableListOf<ServiceAreaBatchRule>(
            ServiceAreaBatchRequiredNameRule("service area batch needs an entry"),
            ServiceAreaBatchSummaryLengthRule(16),
            ServiceAreaBatchRequiredNotesRule("service area batch needs this field"),
            ServiceAreaBatchRequiredCodeRule("service area batch needs an entry"),
            ServiceAreaBatchCodeLengthRule(32),
        ),
    )
}
