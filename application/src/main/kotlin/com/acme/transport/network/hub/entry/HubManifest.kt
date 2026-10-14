package com.acme.transport.network.hub.entry

import com.acme.transport.kit.text.normalize

/** Where a hub manifest is in its life cycle. */
enum class HubManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A hub manifest as the hub team keeps it. */
data class HubManifest(
    var code: String,
    var label: String,
    var endsAt: Int,
    var segment: String,
    var category: String,
    var source: String,
    var status: HubManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubManifestStatus): Boolean {
        when (status) {
            HubManifestStatus.CREATED -> return next == HubManifestStatus.RETIRED
            HubManifestStatus.VALIDATED -> {
                when (next) {
                    HubManifestStatus.RETIRED -> return true
                }
                return false
            }
            HubManifestStatus.PUBLISHED -> return next == HubManifestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: HubManifestStatus) {
        if (!canMoveTo(next)) {
            error("hub manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): HubManifest {
        return HubManifest(
            code = code,
            label = label,
            endsAt = endsAt,
            segment = segment,
            category = category,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a hub manifest in status created. */
fun newHubManifest(code: String, label: String): HubManifest {
    return HubManifest(
        code = code,
        label = label,
        endsAt = 0,
        segment = "",
        category = "",
        source = "",
        status = HubManifestStatus.CREATED,
    )
}
