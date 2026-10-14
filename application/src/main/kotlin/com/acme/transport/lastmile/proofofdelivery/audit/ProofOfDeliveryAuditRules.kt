package com.acme.transport.lastmile.proofofdelivery.audit

/** A rule a proof of delivery audit must pass. Check returns its problems, or none. */
interface ProofOfDeliveryAuditRule {
    fun check(item: ProofOfDeliveryAudit): MutableList<String>
}

data class ProofOfDeliveryAuditPriorityRangeRule(var min: Int, var max: Int) : ProofOfDeliveryAuditRule {
    override fun check(item: ProofOfDeliveryAudit): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAuditCodeLengthRule(var maxLength: Int) : ProofOfDeliveryAuditRule {
    override fun check(item: ProofOfDeliveryAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAuditMarginRangeRule(var min: Double, var max: Double) : ProofOfDeliveryAuditRule {
    override fun check(item: ProofOfDeliveryAudit): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliveryAuditRequiredCodeRule(var message: String) : ProofOfDeliveryAuditRule {
    override fun check(item: ProofOfDeliveryAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAuditBatchSizeRangeRule(var min: Int, var max: Int) : ProofOfDeliveryAuditRule {
    override fun check(item: ProofOfDeliveryAudit): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a proof of delivery audit. */
data class ProofOfDeliveryAuditValidator(var rules: MutableList<ProofOfDeliveryAuditRule>) {
    fun validate(item: ProofOfDeliveryAudit): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryAuditValidator(): ProofOfDeliveryAuditValidator {
    return ProofOfDeliveryAuditValidator(
        mutableListOf<ProofOfDeliveryAuditRule>(
            ProofOfDeliveryAuditPriorityRangeRule(1000, 86400),
            ProofOfDeliveryAuditCodeLengthRule(86400),
            ProofOfDeliveryAuditMarginRangeRule(7.4, 2529.7),
            ProofOfDeliveryAuditRequiredCodeRule("proof of delivery audit needs a value"),
            ProofOfDeliveryAuditBatchSizeRangeRule(16, 16),
        ),
    )
}
