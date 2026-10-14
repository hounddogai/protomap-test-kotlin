package com.acme.transport.freightpay.dispute.group

/** A rule a global dispute reservation must pass. Check returns its problems, or none. */
interface GlobalDisputeReservationRule {
    fun check(item: GlobalDisputeReservation): MutableList<String>
}

data class GlobalDisputeReservationNameLengthRule(var maxLength: Int) : GlobalDisputeReservationRule {
    override fun check(item: GlobalDisputeReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeReservationOwnerIdLengthRule(var maxLength: Int) : GlobalDisputeReservationRule {
    override fun check(item: GlobalDisputeReservation): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeReservationRequiredCodeRule(var message: String) : GlobalDisputeReservationRule {
    override fun check(item: GlobalDisputeReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeReservationCodePrefixRule(var prefix: String) : GlobalDisputeReservationRule {
    override fun check(item: GlobalDisputeReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeReservationRequiredRegionRule(var message: String) : GlobalDisputeReservationRule {
    override fun check(item: GlobalDisputeReservation): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeReservationCodeLengthRule(var maxLength: Int) : GlobalDisputeReservationRule {
    override fun check(item: GlobalDisputeReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global dispute reservation. */
data class GlobalDisputeReservationValidator(var rules: MutableList<GlobalDisputeReservationRule>) {
    fun validate(item: GlobalDisputeReservation): MutableList<String> {
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

    fun requireValid(item: GlobalDisputeReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDisputeReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDisputeReservationValidator(): GlobalDisputeReservationValidator {
    return GlobalDisputeReservationValidator(
        mutableListOf<GlobalDisputeReservationRule>(
            GlobalDisputeReservationNameLengthRule(3600),
            GlobalDisputeReservationOwnerIdLengthRule(3600),
            GlobalDisputeReservationRequiredCodeRule("global dispute reservation needs a value"),
            GlobalDisputeReservationCodePrefixRule("X"),
            GlobalDisputeReservationRequiredRegionRule("global dispute reservation needs an entry"),
            GlobalDisputeReservationCodeLengthRule(86400),
        ),
    )
}
