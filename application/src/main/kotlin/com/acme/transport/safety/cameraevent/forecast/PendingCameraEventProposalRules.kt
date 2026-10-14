package com.acme.transport.safety.cameraevent.forecast

/** A rule a pending camera event proposal must pass. Check returns its problems, or none. */
interface PendingCameraEventProposalRule {
    fun check(item: PendingCameraEventProposal): MutableList<String>
}

data class PendingCameraEventProposalCeilingRangeRule(
    var min: Double,
    var max: Double,
) : PendingCameraEventProposalRule {
    override fun check(item: PendingCameraEventProposal): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventProposalCapacityRangeRule(
    var min: Int,
    var max: Int,
) : PendingCameraEventProposalRule {
    override fun check(item: PendingCameraEventProposal): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventProposalRequiredCodeRule(var message: String) : PendingCameraEventProposalRule {
    override fun check(item: PendingCameraEventProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventProposalRequiredNameRule(var message: String) : PendingCameraEventProposalRule {
    override fun check(item: PendingCameraEventProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventProposalShiftCodeLengthRule(var maxLength: Int) : PendingCameraEventProposalRule {
    override fun check(item: PendingCameraEventProposal): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventProposalCodePrefixRule(var prefix: String) : PendingCameraEventProposalRule {
    override fun check(item: PendingCameraEventProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending camera event proposal. */
data class PendingCameraEventProposalValidator(var rules: MutableList<PendingCameraEventProposalRule>) {
    fun validate(item: PendingCameraEventProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCameraEventProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCameraEventProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCameraEventProposalValidator(): PendingCameraEventProposalValidator {
    return PendingCameraEventProposalValidator(
        mutableListOf<PendingCameraEventProposalRule>(
            PendingCameraEventProposalCeilingRangeRule(8.5, 3003.8),
            PendingCameraEventProposalCapacityRangeRule(64, 120),
            PendingCameraEventProposalRequiredCodeRule("pending camera event proposal needs an entry"),
            PendingCameraEventProposalRequiredNameRule("pending camera event proposal needs an entry"),
            PendingCameraEventProposalShiftCodeLengthRule(80),
            PendingCameraEventProposalCodePrefixRule("X"),
        ),
    )
}
