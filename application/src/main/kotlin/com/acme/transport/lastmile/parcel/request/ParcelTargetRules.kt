package com.acme.transport.lastmile.parcel.request

/** A rule a parcel target must pass. Check returns its problems, or none. */
interface ParcelTargetRule {
    fun check(item: ParcelTarget): MutableList<String>
}

data class ParcelTargetRequiredCodeRule(var message: String) : ParcelTargetRule {
    override fun check(item: ParcelTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelTargetCodePrefixRule(var prefix: String) : ParcelTargetRule {
    override fun check(item: ParcelTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ParcelTargetCodeLengthRule(var maxLength: Int) : ParcelTargetRule {
    override fun check(item: ParcelTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelTargetRequiredLaneRule(var message: String) : ParcelTargetRule {
    override fun check(item: ParcelTarget): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel target. */
data class ParcelTargetValidator(var rules: MutableList<ParcelTargetRule>) {
    fun validate(item: ParcelTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelTargetValidator(): ParcelTargetValidator {
    return ParcelTargetValidator(
        mutableListOf<ParcelTargetRule>(
            ParcelTargetRequiredCodeRule("parcel target needs this field"),
            ParcelTargetCodePrefixRule("Q"),
            ParcelTargetCodeLengthRule(255),
            ParcelTargetRequiredLaneRule("parcel target needs this field"),
        ),
    )
}
