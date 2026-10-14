package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.text.normalize

/** A rule a rolling dock door agreement must pass. Check returns its problems, or none. */
interface RollingDockDoorAgreementRule {
    fun check(item: RollingDockDoorAgreement): MutableList<String>
}

data class RollingDockDoorAgreementVisibleGuardRule(var limit: Double) : RollingDockDoorAgreementRule {
    override fun check(item: RollingDockDoorAgreement): MutableList<String> {
        if (item.visible && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorAgreementPhoneLengthRule(var maxLength: Int) : RollingDockDoorAgreementRule {
    override fun check(item: RollingDockDoorAgreement): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorAgreementTitleLengthRule(var maxLength: Int) : RollingDockDoorAgreementRule {
    override fun check(item: RollingDockDoorAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorAgreementRequiredPhoneRule(var message: String) : RollingDockDoorAgreementRule {
    override fun check(item: RollingDockDoorAgreement): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorAgreementAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : RollingDockDoorAgreementRule {
    override fun check(item: RollingDockDoorAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dock door agreement. */
data class RollingDockDoorAgreementValidator(var rules: MutableList<RollingDockDoorAgreementRule>) {
    fun validate(item: RollingDockDoorAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDockDoorAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDockDoorAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDockDoorAgreementValidator(): RollingDockDoorAgreementValidator {
    return RollingDockDoorAgreementValidator(
        mutableListOf<RollingDockDoorAgreementRule>(
            RollingDockDoorAgreementVisibleGuardRule(7.2),
            RollingDockDoorAgreementPhoneLengthRule(16),
            RollingDockDoorAgreementTitleLengthRule(86400),
            RollingDockDoorAgreementRequiredPhoneRule("rolling dock door agreement needs this field"),
            RollingDockDoorAgreementAllowedExternalRefRule(mutableListOf<String>()),
        ),
    )
}
