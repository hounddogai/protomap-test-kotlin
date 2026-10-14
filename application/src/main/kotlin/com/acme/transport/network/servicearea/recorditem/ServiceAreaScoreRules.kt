package com.acme.transport.network.servicearea.recorditem

/** A rule a service area score must pass. Check returns its problems, or none. */
interface ServiceAreaScoreRule {
    fun check(item: ServiceAreaScore): MutableList<String>
}

data class ServiceAreaScoreCapacityRangeRule(var min: Int, var max: Int) : ServiceAreaScoreRule {
    override fun check(item: ServiceAreaScore): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaScoreRetriesRangeRule(var min: Int, var max: Int) : ServiceAreaScoreRule {
    override fun check(item: ServiceAreaScore): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaScoreBatchSizeRangeRule(var min: Int, var max: Int) : ServiceAreaScoreRule {
    override fun check(item: ServiceAreaScore): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a service area score. */
data class ServiceAreaScoreValidator(var rules: MutableList<ServiceAreaScoreRule>) {
    fun validate(item: ServiceAreaScore): MutableList<String> {
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

    fun requireValid(item: ServiceAreaScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaScore>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaScoreValidator(): ServiceAreaScoreValidator {
    return ServiceAreaScoreValidator(
        mutableListOf<ServiceAreaScoreRule>(
            ServiceAreaScoreCapacityRangeRule(255, 86400),
            ServiceAreaScoreRetriesRangeRule(1000, 86400),
            ServiceAreaScoreBatchSizeRangeRule(16, 3600),
        ),
    )
}
