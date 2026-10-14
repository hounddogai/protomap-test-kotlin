package com.acme.transport.fleetops.leasereturn.request

/** A rule a rolling lease return request must pass. Check returns its problems, or none. */
interface RollingLeaseReturnRequestRule {
    fun check(item: RollingLeaseReturnRequest): MutableList<String>
}

data class RollingLeaseReturnRequestRequiredCodeRule(var message: String) : RollingLeaseReturnRequestRule {
    override fun check(item: RollingLeaseReturnRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingLeaseReturnRequestCodeLengthRule(var maxLength: Int) : RollingLeaseReturnRequestRule {
    override fun check(item: RollingLeaseReturnRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingLeaseReturnRequestRequiredNameRule(var message: String) : RollingLeaseReturnRequestRule {
    override fun check(item: RollingLeaseReturnRequest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling lease return request. */
data class RollingLeaseReturnRequestValidator(var rules: MutableList<RollingLeaseReturnRequestRule>) {
    fun validate(item: RollingLeaseReturnRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingLeaseReturnRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingLeaseReturnRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingLeaseReturnRequestValidator(): RollingLeaseReturnRequestValidator {
    return RollingLeaseReturnRequestValidator(
        mutableListOf<RollingLeaseReturnRequestRule>(
            RollingLeaseReturnRequestRequiredCodeRule("rolling lease return request needs a value"),
            RollingLeaseReturnRequestCodeLengthRule(80),
            RollingLeaseReturnRequestRequiredNameRule("rolling lease return request needs a value"),
        ),
    )
}
