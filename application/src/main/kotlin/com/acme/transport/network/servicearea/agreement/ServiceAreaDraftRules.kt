package com.acme.transport.network.servicearea.agreement

/** A rule a service area draft must pass. Check returns its problems, or none. */
interface ServiceAreaDraftRule {
    fun check(item: ServiceAreaDraft): MutableList<String>
}

data class ServiceAreaDraftFuelLitersRangeRule(var min: Double, var max: Double) : ServiceAreaDraftRule {
    override fun check(item: ServiceAreaDraft): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaDraftRequiredReviewerRule(var message: String) : ServiceAreaDraftRule {
    override fun check(item: ServiceAreaDraft): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaDraftCodeLengthRule(var maxLength: Int) : ServiceAreaDraftRule {
    override fun check(item: ServiceAreaDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaDraftBatchSizeRangeRule(var min: Int, var max: Int) : ServiceAreaDraftRule {
    override fun check(item: ServiceAreaDraft): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaDraftNameLengthRule(var maxLength: Int) : ServiceAreaDraftRule {
    override fun check(item: ServiceAreaDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaDraftReviewerLengthRule(var maxLength: Int) : ServiceAreaDraftRule {
    override fun check(item: ServiceAreaDraft): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area draft. */
data class ServiceAreaDraftValidator(var rules: MutableList<ServiceAreaDraftRule>) {
    fun validate(item: ServiceAreaDraft): MutableList<String> {
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

    fun requireValid(item: ServiceAreaDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaDraftValidator(): ServiceAreaDraftValidator {
    return ServiceAreaDraftValidator(
        mutableListOf<ServiceAreaDraftRule>(
            ServiceAreaDraftFuelLitersRangeRule(4.4, 3598.0),
            ServiceAreaDraftRequiredReviewerRule("service area draft needs this field"),
            ServiceAreaDraftCodeLengthRule(64),
            ServiceAreaDraftBatchSizeRangeRule(255, 3600),
            ServiceAreaDraftNameLengthRule(255),
            ServiceAreaDraftReviewerLengthRule(32),
        ),
    )
}
