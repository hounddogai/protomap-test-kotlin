package com.acme.transport.freightpay.accessorial.reservation

/** A rule a pending accessorial group must pass. Check returns its problems, or none. */
interface PendingAccessorialGroupRule {
    fun check(item: PendingAccessorialGroup): MutableList<String>
}

data class PendingAccessorialGroupRequiredCodeRule(var message: String) : PendingAccessorialGroupRule {
    override fun check(item: PendingAccessorialGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialGroupRequiredTimezoneRule(var message: String) : PendingAccessorialGroupRule {
    override fun check(item: PendingAccessorialGroup): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialGroupCodePrefixRule(var prefix: String) : PendingAccessorialGroupRule {
    override fun check(item: PendingAccessorialGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingAccessorialGroupNameLengthRule(var maxLength: Int) : PendingAccessorialGroupRule {
    override fun check(item: PendingAccessorialGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending accessorial group. */
data class PendingAccessorialGroupValidator(var rules: MutableList<PendingAccessorialGroupRule>) {
    fun validate(item: PendingAccessorialGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingAccessorialGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAccessorialGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAccessorialGroupValidator(): PendingAccessorialGroupValidator {
    return PendingAccessorialGroupValidator(
        mutableListOf<PendingAccessorialGroupRule>(
            PendingAccessorialGroupRequiredCodeRule("pending accessorial group needs an entry"),
            PendingAccessorialGroupRequiredTimezoneRule("pending accessorial group needs a value"),
            PendingAccessorialGroupCodePrefixRule("OPS"),
            PendingAccessorialGroupNameLengthRule(80),
        ),
    )
}
