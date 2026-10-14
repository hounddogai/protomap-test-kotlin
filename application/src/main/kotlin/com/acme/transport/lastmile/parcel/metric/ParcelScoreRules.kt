package com.acme.transport.lastmile.parcel.metric

/** A rule a parcel score must pass. Check returns its problems, or none. */
interface ParcelScoreRule {
    fun check(item: ParcelScore): MutableList<String>
}

data class ParcelScoreRequiredTitleRule(var message: String) : ParcelScoreRule {
    override fun check(item: ParcelScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelScoreChannelLengthRule(var maxLength: Int) : ParcelScoreRule {
    override fun check(item: ParcelScore): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelScoreRequiredContactEmailRule(var message: String) : ParcelScoreRule {
    override fun check(item: ParcelScore): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel score. */
data class ParcelScoreValidator(var rules: MutableList<ParcelScoreRule>) {
    fun validate(item: ParcelScore): MutableList<String> {
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

    fun requireValid(item: ParcelScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelScore>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelScoreValidator(): ParcelScoreValidator {
    return ParcelScoreValidator(
        mutableListOf<ParcelScoreRule>(
            ParcelScoreRequiredTitleRule("parcel score needs an entry"),
            ParcelScoreChannelLengthRule(120),
            ParcelScoreRequiredContactEmailRule("parcel score needs an entry"),
        ),
    )
}
