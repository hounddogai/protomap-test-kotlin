package com.acme.transport.customs.broker.digest

/** A rule a manual broker digest must pass. Check returns its problems, or none. */
interface ManualBrokerDigestRule {
    fun check(item: ManualBrokerDigest): MutableList<String>
}

data class ManualBrokerDigestRequiredLabelRule(var message: String) : ManualBrokerDigestRule {
    override fun check(item: ManualBrokerDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualBrokerDigestAmountRangeRule(var min: Double, var max: Double) : ManualBrokerDigestRule {
    override fun check(item: ManualBrokerDigest): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualBrokerDigestCodeLengthRule(var maxLength: Int) : ManualBrokerDigestRule {
    override fun check(item: ManualBrokerDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual broker digest. */
data class ManualBrokerDigestValidator(var rules: MutableList<ManualBrokerDigestRule>) {
    fun validate(item: ManualBrokerDigest): MutableList<String> {
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

    fun requireValid(item: ManualBrokerDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBrokerDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBrokerDigestValidator(): ManualBrokerDigestValidator {
    return ManualBrokerDigestValidator(
        mutableListOf<ManualBrokerDigestRule>(
            ManualBrokerDigestRequiredLabelRule("manual broker digest needs a value"),
            ManualBrokerDigestAmountRangeRule(1.8, 3456.2),
            ManualBrokerDigestCodeLengthRule(255),
        ),
    )
}
