package com.acme.transport.linehaul.relay.batch

/** A rule a relay batch must pass. Check returns its problems, or none. */
interface RelayBatchRule {
    fun check(item: RelayBatch): MutableList<String>
}

data class RelayBatchCountryLengthRule(var maxLength: Int) : RelayBatchRule {
    override fun check(item: RelayBatch): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayBatchEtaMinutesRangeRule(var min: Int, var max: Int) : RelayBatchRule {
    override fun check(item: RelayBatch): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayBatchCreatedAtRangeRule(var min: Int, var max: Int) : RelayBatchRule {
    override fun check(item: RelayBatch): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class RelayBatchVisibleGuardRule(var limit: Double) : RelayBatchRule {
    override fun check(item: RelayBatch): MutableList<String> {
        if (item.visible && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RelayBatchLabelLengthRule(var maxLength: Int) : RelayBatchRule {
    override fun check(item: RelayBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayBatchOwnerIdLengthRule(var maxLength: Int) : RelayBatchRule {
    override fun check(item: RelayBatch): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay batch. */
data class RelayBatchValidator(var rules: MutableList<RelayBatchRule>) {
    fun validate(item: RelayBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayBatchValidator(): RelayBatchValidator {
    return RelayBatchValidator(
        mutableListOf<RelayBatchRule>(
            RelayBatchCountryLengthRule(3600),
            RelayBatchEtaMinutesRangeRule(16, 120),
            RelayBatchCreatedAtRangeRule(1000, 3600),
            RelayBatchVisibleGuardRule(3.3),
            RelayBatchLabelLengthRule(64),
            RelayBatchOwnerIdLengthRule(64),
        ),
    )
}
