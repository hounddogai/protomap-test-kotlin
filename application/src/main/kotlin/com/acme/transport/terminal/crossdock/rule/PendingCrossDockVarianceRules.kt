package com.acme.transport.terminal.crossdock.rule

/** A rule a pending cross dock variance must pass. Check returns its problems, or none. */
interface PendingCrossDockVarianceRule {
    fun check(item: PendingCrossDockVariance): MutableList<String>
}

data class PendingCrossDockVarianceRequiredCodeRule(var message: String) : PendingCrossDockVarianceRule {
    override fun check(item: PendingCrossDockVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockVarianceCodePrefixRule(var prefix: String) : PendingCrossDockVarianceRule {
    override fun check(item: PendingCrossDockVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockVarianceDepotLengthRule(var maxLength: Int) : PendingCrossDockVarianceRule {
    override fun check(item: PendingCrossDockVariance): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCrossDockVarianceRequiredNameRule(var message: String) : PendingCrossDockVarianceRule {
    override fun check(item: PendingCrossDockVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending cross dock variance. */
data class PendingCrossDockVarianceValidator(var rules: MutableList<PendingCrossDockVarianceRule>) {
    fun validate(item: PendingCrossDockVariance): MutableList<String> {
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

    fun requireValid(item: PendingCrossDockVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCrossDockVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCrossDockVarianceValidator(): PendingCrossDockVarianceValidator {
    return PendingCrossDockVarianceValidator(
        mutableListOf<PendingCrossDockVarianceRule>(
            PendingCrossDockVarianceRequiredCodeRule("pending cross dock variance needs this field"),
            PendingCrossDockVarianceCodePrefixRule("ACM"),
            PendingCrossDockVarianceDepotLengthRule(16),
            PendingCrossDockVarianceRequiredNameRule("pending cross dock variance needs a value"),
        ),
    )
}
