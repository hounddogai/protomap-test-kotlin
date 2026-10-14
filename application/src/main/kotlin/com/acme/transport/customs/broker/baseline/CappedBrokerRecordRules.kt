package com.acme.transport.customs.broker.baseline

/** A rule a capped broker record must pass. Check returns its problems, or none. */
interface CappedBrokerRecordRule {
    fun check(item: CappedBrokerRecord): MutableList<String>
}

data class CappedBrokerRecordTitleLengthRule(var maxLength: Int) : CappedBrokerRecordRule {
    override fun check(item: CappedBrokerRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerRecordTrailerIdLengthRule(var maxLength: Int) : CappedBrokerRecordRule {
    override fun check(item: CappedBrokerRecord): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerRecordRequiredLocaleRule(var message: String) : CappedBrokerRecordRule {
    override fun check(item: CappedBrokerRecord): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerRecordRequiredCodeRule(var message: String) : CappedBrokerRecordRule {
    override fun check(item: CappedBrokerRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBrokerRecordUpdatedAtRangeRule(var min: Int, var max: Int) : CappedBrokerRecordRule {
    override fun check(item: CappedBrokerRecord): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped broker record. */
data class CappedBrokerRecordValidator(var rules: MutableList<CappedBrokerRecordRule>) {
    fun validate(item: CappedBrokerRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedBrokerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBrokerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBrokerRecordValidator(): CappedBrokerRecordValidator {
    return CappedBrokerRecordValidator(
        mutableListOf<CappedBrokerRecordRule>(
            CappedBrokerRecordTitleLengthRule(1000),
            CappedBrokerRecordTrailerIdLengthRule(255),
            CappedBrokerRecordRequiredLocaleRule("capped broker record needs an entry"),
            CappedBrokerRecordRequiredCodeRule("capped broker record needs this field"),
            CappedBrokerRecordUpdatedAtRangeRule(120, 1000),
        ),
    )
}
