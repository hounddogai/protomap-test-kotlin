package com.acme.transport.lastmile.drop.dropnotice

/** A rule a drop agreement must pass. Check returns its problems, or none. */
interface DropAgreementRule {
    fun check(item: DropAgreement): MutableList<String>
}

data class DropAgreementRequiredDescriptionRule(var message: String) : DropAgreementRule {
    override fun check(item: DropAgreement): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropAgreementMinimumRangeRule(var min: Double, var max: Double) : DropAgreementRule {
    override fun check(item: DropAgreement): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DropAgreementRequiredLabelRule(var message: String) : DropAgreementRule {
    override fun check(item: DropAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropAgreementOwnerIdLengthRule(var maxLength: Int) : DropAgreementRule {
    override fun check(item: DropAgreement): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropAgreementRequiredOwnerIdRule(var message: String) : DropAgreementRule {
    override fun check(item: DropAgreement): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropAgreementLabelLengthRule(var maxLength: Int) : DropAgreementRule {
    override fun check(item: DropAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop agreement. */
data class DropAgreementValidator(var rules: MutableList<DropAgreementRule>) {
    fun validate(item: DropAgreement): MutableList<String> {
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

    fun requireValid(item: DropAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDropAgreementValidator(): DropAgreementValidator {
    return DropAgreementValidator(
        mutableListOf<DropAgreementRule>(
            DropAgreementRequiredDescriptionRule("drop agreement needs an entry"),
            DropAgreementMinimumRangeRule(2.6, 2402.0),
            DropAgreementRequiredLabelRule("drop agreement needs this field"),
            DropAgreementOwnerIdLengthRule(64),
            DropAgreementRequiredOwnerIdRule("drop agreement needs this field"),
            DropAgreementLabelLengthRule(1000),
        ),
    )
}
