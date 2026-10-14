package com.acme.transport.fleetops.leasereturn.returnforecast

/** A rule a primary lease return request must pass. Check returns its problems, or none. */
interface PrimaryLeaseReturnRequestRule {
    fun check(item: PrimaryLeaseReturnRequest): MutableList<String>
}

data class PrimaryLeaseReturnRequestRequiredTitleRule(var message: String) : PrimaryLeaseReturnRequestRule {
    override fun check(item: PrimaryLeaseReturnRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLeaseReturnRequestTitleLengthRule(var maxLength: Int) : PrimaryLeaseReturnRequestRule {
    override fun check(item: PrimaryLeaseReturnRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLeaseReturnRequestRequiredTimezoneRule(var message: String) : PrimaryLeaseReturnRequestRule {
    override fun check(item: PrimaryLeaseReturnRequest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLeaseReturnRequestRequiredCodeRule(var message: String) : PrimaryLeaseReturnRequestRule {
    override fun check(item: PrimaryLeaseReturnRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLeaseReturnRequestPhoneLengthRule(var maxLength: Int) : PrimaryLeaseReturnRequestRule {
    override fun check(item: PrimaryLeaseReturnRequest): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary lease return request. */
data class PrimaryLeaseReturnRequestValidator(var rules: MutableList<PrimaryLeaseReturnRequestRule>) {
    fun validate(item: PrimaryLeaseReturnRequest): MutableList<String> {
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

    fun requireValid(item: PrimaryLeaseReturnRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryLeaseReturnRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryLeaseReturnRequestValidator(): PrimaryLeaseReturnRequestValidator {
    return PrimaryLeaseReturnRequestValidator(
        mutableListOf<PrimaryLeaseReturnRequestRule>(
            PrimaryLeaseReturnRequestRequiredTitleRule("primary lease return request needs this field"),
            PrimaryLeaseReturnRequestTitleLengthRule(64),
            PrimaryLeaseReturnRequestRequiredTimezoneRule("primary lease return request needs this field"),
            PrimaryLeaseReturnRequestRequiredCodeRule("primary lease return request needs an entry"),
            PrimaryLeaseReturnRequestPhoneLengthRule(120),
        ),
    )
}
