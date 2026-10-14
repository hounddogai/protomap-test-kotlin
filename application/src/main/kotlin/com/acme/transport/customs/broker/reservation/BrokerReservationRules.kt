package com.acme.transport.customs.broker.reservation

/** A rule a broker reservation must pass. Check returns its problems, or none. */
interface BrokerReservationRule {
    fun check(item: BrokerReservation): MutableList<String>
}

data class BrokerReservationRequiredOwnerIdRule(var message: String) : BrokerReservationRule {
    override fun check(item: BrokerReservation): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerReservationRequiredCodeRule(var message: String) : BrokerReservationRule {
    override fun check(item: BrokerReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerReservationCodePrefixRule(var prefix: String) : BrokerReservationRule {
    override fun check(item: BrokerReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerReservationCodeLengthRule(var maxLength: Int) : BrokerReservationRule {
    override fun check(item: BrokerReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerReservationRequiredTitleRule(var message: String) : BrokerReservationRule {
    override fun check(item: BrokerReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker reservation. */
data class BrokerReservationValidator(var rules: MutableList<BrokerReservationRule>) {
    fun validate(item: BrokerReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerReservationValidator(): BrokerReservationValidator {
    return BrokerReservationValidator(
        mutableListOf<BrokerReservationRule>(
            BrokerReservationRequiredOwnerIdRule("broker reservation needs this field"),
            BrokerReservationRequiredCodeRule("broker reservation needs an entry"),
            BrokerReservationCodePrefixRule("ACM"),
            BrokerReservationCodeLengthRule(80),
            BrokerReservationRequiredTitleRule("broker reservation needs a value"),
        ),
    )
}
