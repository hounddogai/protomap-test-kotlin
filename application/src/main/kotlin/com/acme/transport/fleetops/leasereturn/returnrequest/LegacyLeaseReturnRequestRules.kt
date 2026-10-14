package com.acme.transport.fleetops.leasereturn.returnrequest

/** A rule a legacy lease return request must pass. Check returns its problems, or none. */
interface LegacyLeaseReturnRequestRule {
    fun check(item: LegacyLeaseReturnRequest): MutableList<String>
}

data class LegacyLeaseReturnRequestCodeLengthRule(var maxLength: Int) : LegacyLeaseReturnRequestRule {
    override fun check(item: LegacyLeaseReturnRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnRequestCodePrefixRule(var prefix: String) : LegacyLeaseReturnRequestRule {
    override fun check(item: LegacyLeaseReturnRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnRequestShiftCodeLengthRule(var maxLength: Int) : LegacyLeaseReturnRequestRule {
    override fun check(item: LegacyLeaseReturnRequest): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lease return request. */
data class LegacyLeaseReturnRequestValidator(var rules: MutableList<LegacyLeaseReturnRequestRule>) {
    fun validate(item: LegacyLeaseReturnRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyLeaseReturnRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLeaseReturnRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLeaseReturnRequestValidator(): LegacyLeaseReturnRequestValidator {
    return LegacyLeaseReturnRequestValidator(
        mutableListOf<LegacyLeaseReturnRequestRule>(
            LegacyLeaseReturnRequestCodeLengthRule(80),
            LegacyLeaseReturnRequestCodePrefixRule("ACM"),
            LegacyLeaseReturnRequestShiftCodeLengthRule(80),
        ),
    )
}
