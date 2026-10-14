package com.acme.transport.terminal.traileryard.template

/** A rule a rolling trailer yard window must pass. Check returns its problems, or none. */
interface RollingTrailerYardWindowRule {
    fun check(item: RollingTrailerYardWindow): MutableList<String>
}

data class RollingTrailerYardWindowRequiredChecksumRule(var message: String) : RollingTrailerYardWindowRule {
    override fun check(item: RollingTrailerYardWindow): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardWindowChecksumLengthRule(var maxLength: Int) : RollingTrailerYardWindowRule {
    override fun check(item: RollingTrailerYardWindow): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardWindowRequiredCodeRule(var message: String) : RollingTrailerYardWindowRule {
    override fun check(item: RollingTrailerYardWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardWindowCodeLengthRule(var maxLength: Int) : RollingTrailerYardWindowRule {
    override fun check(item: RollingTrailerYardWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardWindowRequiredChannelRule(var message: String) : RollingTrailerYardWindowRule {
    override fun check(item: RollingTrailerYardWindow): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling trailer yard window. */
data class RollingTrailerYardWindowValidator(var rules: MutableList<RollingTrailerYardWindowRule>) {
    fun validate(item: RollingTrailerYardWindow): MutableList<String> {
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

    fun requireValid(item: RollingTrailerYardWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTrailerYardWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTrailerYardWindowValidator(): RollingTrailerYardWindowValidator {
    return RollingTrailerYardWindowValidator(
        mutableListOf<RollingTrailerYardWindowRule>(
            RollingTrailerYardWindowRequiredChecksumRule("rolling trailer yard window needs this field"),
            RollingTrailerYardWindowChecksumLengthRule(80),
            RollingTrailerYardWindowRequiredCodeRule("rolling trailer yard window needs a value"),
            RollingTrailerYardWindowCodeLengthRule(3600),
            RollingTrailerYardWindowRequiredChannelRule("rolling trailer yard window needs this field"),
        ),
    )
}
