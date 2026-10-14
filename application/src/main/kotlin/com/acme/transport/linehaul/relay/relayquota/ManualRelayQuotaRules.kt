package com.acme.transport.linehaul.relay.relayquota

/** A rule a manual relay quota must pass. Check returns its problems, or none. */
interface ManualRelayQuotaRule {
    fun check(item: ManualRelayQuota): MutableList<String>
}

data class ManualRelayQuotaCodePrefixRule(var prefix: String) : ManualRelayQuotaRule {
    override fun check(item: ManualRelayQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayQuotaStopCountRangeRule(var min: Int, var max: Int) : ManualRelayQuotaRule {
    override fun check(item: ManualRelayQuota): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayQuotaRequiredCodeRule(var message: String) : ManualRelayQuotaRule {
    override fun check(item: ManualRelayQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayQuotaTitleLengthRule(var maxLength: Int) : ManualRelayQuotaRule {
    override fun check(item: ManualRelayQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual relay quota. */
data class ManualRelayQuotaValidator(var rules: MutableList<ManualRelayQuotaRule>) {
    fun validate(item: ManualRelayQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRelayQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRelayQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRelayQuotaValidator(): ManualRelayQuotaValidator {
    return ManualRelayQuotaValidator(
        mutableListOf<ManualRelayQuotaRule>(
            ManualRelayQuotaCodePrefixRule("Q"),
            ManualRelayQuotaStopCountRangeRule(3600, 86400),
            ManualRelayQuotaRequiredCodeRule("manual relay quota needs an entry"),
            ManualRelayQuotaTitleLengthRule(80),
        ),
    )
}
