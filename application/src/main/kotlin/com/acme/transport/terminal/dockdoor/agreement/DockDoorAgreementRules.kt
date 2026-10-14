package com.acme.transport.terminal.dockdoor.agreement

/** A rule a dock door agreement must pass. Check returns its problems, or none. */
interface DockDoorAgreementRule {
    fun check(item: DockDoorAgreement): MutableList<String>
}

data class DockDoorAgreementRequiredRegionRule(var message: String) : DockDoorAgreementRule {
    override fun check(item: DockDoorAgreement): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorAgreementRevisionRangeRule(var min: Int, var max: Int) : DockDoorAgreementRule {
    override fun check(item: DockDoorAgreement): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class DockDoorAgreementRequiredNameRule(var message: String) : DockDoorAgreementRule {
    override fun check(item: DockDoorAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorAgreementRequiredCodeRule(var message: String) : DockDoorAgreementRule {
    override fun check(item: DockDoorAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorAgreementCodePrefixRule(var prefix: String) : DockDoorAgreementRule {
    override fun check(item: DockDoorAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door agreement. */
data class DockDoorAgreementValidator(var rules: MutableList<DockDoorAgreementRule>) {
    fun validate(item: DockDoorAgreement): MutableList<String> {
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

    fun requireValid(item: DockDoorAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorAgreementValidator(): DockDoorAgreementValidator {
    return DockDoorAgreementValidator(
        mutableListOf<DockDoorAgreementRule>(
            DockDoorAgreementRequiredRegionRule("dock door agreement needs an entry"),
            DockDoorAgreementRevisionRangeRule(120, 255),
            DockDoorAgreementRequiredNameRule("dock door agreement needs a value"),
            DockDoorAgreementRequiredCodeRule("dock door agreement needs a value"),
            DockDoorAgreementCodePrefixRule("ACM"),
        ),
    )
}
