package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.text.normalize

/** Where a global broker summary is in its life cycle. */
enum class GlobalBrokerSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global broker summary, with what the planners need to act on it. */
data class GlobalBrokerSummary(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var priority: Int,
    var source: String,
    var summary: String,
    var ownerId: String,
    var status: GlobalBrokerSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalBrokerSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalBrokerSummaryStatus): Boolean {
        when (status) {
            GlobalBrokerSummaryStatus.CREATED -> return next == GlobalBrokerSummaryStatus.PUBLISHED || next == GlobalBrokerSummaryStatus.RETIRED
            GlobalBrokerSummaryStatus.VALIDATED -> return next == GlobalBrokerSummaryStatus.PUBLISHED || next == GlobalBrokerSummaryStatus.RETIRED
            GlobalBrokerSummaryStatus.PUBLISHED -> {
                when (next) {
                    GlobalBrokerSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalBrokerSummaryStatus) {
        if (!canMoveTo(next)) {
            error("global broker summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): GlobalBrokerSummary {
        return GlobalBrokerSummary(
            code = code,
            name = name,
            tags = tags,
            priority = priority,
            source = source,
            summary = summary,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global broker summary in status created. */
fun newGlobalBrokerSummary(code: String, name: String): GlobalBrokerSummary {
    return GlobalBrokerSummary(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        priority = 0,
        source = "",
        summary = "",
        ownerId = "",
        status = GlobalBrokerSummaryStatus.CREATED,
    )
}
