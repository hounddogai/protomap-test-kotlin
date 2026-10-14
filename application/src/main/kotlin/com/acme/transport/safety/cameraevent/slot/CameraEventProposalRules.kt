package com.acme.transport.safety.cameraevent.slot

/** A rule a camera event proposal must pass. Check returns its problems, or none. */
interface CameraEventProposalRule {
    fun check(item: CameraEventProposal): MutableList<String>
}

data class CameraEventProposalRequiredSourceRule(var message: String) : CameraEventProposalRule {
    override fun check(item: CameraEventProposal): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventProposalTitleLengthRule(var maxLength: Int) : CameraEventProposalRule {
    override fun check(item: CameraEventProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventProposalRequiredTitleRule(var message: String) : CameraEventProposalRule {
    override fun check(item: CameraEventProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event proposal. */
data class CameraEventProposalValidator(var rules: MutableList<CameraEventProposalRule>) {
    fun validate(item: CameraEventProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventProposalValidator(): CameraEventProposalValidator {
    return CameraEventProposalValidator(
        mutableListOf<CameraEventProposalRule>(
            CameraEventProposalRequiredSourceRule("camera event proposal needs an entry"),
            CameraEventProposalTitleLengthRule(86400),
            CameraEventProposalRequiredTitleRule("camera event proposal needs this field"),
        ),
    )
}
