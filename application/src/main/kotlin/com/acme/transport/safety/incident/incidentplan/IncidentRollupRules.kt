package com.acme.transport.safety.incident.incidentplan

/** A rule a incident rollup must pass. Check returns its problems, or none. */
interface IncidentRollupRule {
    fun check(item: IncidentRollup): MutableList<String>
}

data class IncidentRollupRequiredCountryRule(var message: String) : IncidentRollupRule {
    override fun check(item: IncidentRollup): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentRollupRequiredCodeRule(var message: String) : IncidentRollupRule {
    override fun check(item: IncidentRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentRollupChecksumLengthRule(var maxLength: Int) : IncidentRollupRule {
    override fun check(item: IncidentRollup): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentRollupCodeLengthRule(var maxLength: Int) : IncidentRollupRule {
    override fun check(item: IncidentRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident rollup. */
data class IncidentRollupValidator(var rules: MutableList<IncidentRollupRule>) {
    fun validate(item: IncidentRollup): MutableList<String> {
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

    fun requireValid(item: IncidentRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentRollupValidator(): IncidentRollupValidator {
    return IncidentRollupValidator(
        mutableListOf<IncidentRollupRule>(
            IncidentRollupRequiredCountryRule("incident rollup needs an entry"),
            IncidentRollupRequiredCodeRule("incident rollup needs an entry"),
            IncidentRollupChecksumLengthRule(255),
            IncidentRollupCodeLengthRule(86400),
        ),
    )
}
