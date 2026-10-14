package com.acme.transport.lastmile.drop.dropbundle

/** A rule a tiered drop target must pass. Check returns its problems, or none. */
interface TieredDropTargetRule {
    fun check(item: TieredDropTarget): MutableList<String>
}

data class TieredDropTargetTitleLengthRule(var maxLength: Int) : TieredDropTargetRule {
    override fun check(item: TieredDropTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropTargetRequiredCodeRule(var message: String) : TieredDropTargetRule {
    override fun check(item: TieredDropTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDropTargetCodePrefixRule(var prefix: String) : TieredDropTargetRule {
    override fun check(item: TieredDropTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropTargetNotesLengthRule(var maxLength: Int) : TieredDropTargetRule {
    override fun check(item: TieredDropTarget): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropTargetRequiredNotesRule(var message: String) : TieredDropTargetRule {
    override fun check(item: TieredDropTarget): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered drop target. */
data class TieredDropTargetValidator(var rules: MutableList<TieredDropTargetRule>) {
    fun validate(item: TieredDropTarget): MutableList<String> {
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

    fun requireValid(item: TieredDropTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDropTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDropTargetValidator(): TieredDropTargetValidator {
    return TieredDropTargetValidator(
        mutableListOf<TieredDropTargetRule>(
            TieredDropTargetTitleLengthRule(3600),
            TieredDropTargetRequiredCodeRule("tiered drop target needs this field"),
            TieredDropTargetCodePrefixRule("ACM"),
            TieredDropTargetNotesLengthRule(255),
            TieredDropTargetRequiredNotesRule("tiered drop target needs this field"),
        ),
    )
}
