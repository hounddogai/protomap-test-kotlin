package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending dispute manifests by code. */
interface PendingDisputeManifestStore {
    fun find(code: String): PendingDisputeManifest?
    fun save(item: PendingDisputeManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDisputeManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDisputeManifestStore(
    var items: MutableMap<String, PendingDisputeManifest>,
    var order: MutableList<String>,
) : PendingDisputeManifestStore {
    override fun find(code: String): PendingDisputeManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDisputeManifest) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<PendingDisputeManifest> {
        val out = mutableListOf<PendingDisputeManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDisputeManifestStore(): PendingDisputeManifestStore {
    return MemoryPendingDisputeManifestStore(
        mutableMapOf<String, PendingDisputeManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDisputeManifestStore(
    var inner: PendingDisputeManifestStore,
    var cache: TtlCache<PendingDisputeManifest>,
    var hits: Int,
    var misses: Int,
) : PendingDisputeManifestStore {
    override fun find(code: String): PendingDisputeManifest? {
        val hit = (cache.get(code) as PendingDisputeManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDisputeManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDisputeManifest> = inner.list()
}

fun newCachedPendingDisputeManifestStore(inner: PendingDisputeManifestStore, ttl: Int, clock: Clock): PendingDisputeManifestStore {
    return CachedPendingDisputeManifestStore(
        inner = inner,
        cache = newTtlCache<PendingDisputeManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending dispute manifests. Empty fields match all. */
data class PendingDisputeManifestQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDisputeManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDisputeManifest>): MutableList<PendingDisputeManifest> {
        val matched = filter(items) { candidate: PendingDisputeManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDisputeManifests(store: PendingDisputeManifestStore, query: PendingDisputeManifestQuery): MutableList<PendingDisputeManifest> {
    return query.apply(store.list())
}
