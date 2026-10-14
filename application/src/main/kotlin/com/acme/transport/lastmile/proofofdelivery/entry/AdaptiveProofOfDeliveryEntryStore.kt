package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive proof of delivery entries by code. */
interface AdaptiveProofOfDeliveryEntryStore {
    fun find(code: String): AdaptiveProofOfDeliveryEntry?
    fun save(item: AdaptiveProofOfDeliveryEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveProofOfDeliveryEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveProofOfDeliveryEntryStore(
    var items: MutableMap<String, AdaptiveProofOfDeliveryEntry>,
    var order: MutableList<String>,
) : AdaptiveProofOfDeliveryEntryStore {
    override fun find(code: String): AdaptiveProofOfDeliveryEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveProofOfDeliveryEntry) {
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

    override fun list(): MutableList<AdaptiveProofOfDeliveryEntry> {
        val out = mutableListOf<AdaptiveProofOfDeliveryEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveProofOfDeliveryEntryStore(): AdaptiveProofOfDeliveryEntryStore {
    return MemoryAdaptiveProofOfDeliveryEntryStore(
        mutableMapOf<String, AdaptiveProofOfDeliveryEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveProofOfDeliveryEntryStore(
    var inner: AdaptiveProofOfDeliveryEntryStore,
    var cache: TtlCache<AdaptiveProofOfDeliveryEntry>,
    var hits: Int,
    var misses: Int,
) : AdaptiveProofOfDeliveryEntryStore {
    override fun find(code: String): AdaptiveProofOfDeliveryEntry? {
        val hit = (cache.get(code) as AdaptiveProofOfDeliveryEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveProofOfDeliveryEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveProofOfDeliveryEntry> = inner.list()
}

fun newCachedAdaptiveProofOfDeliveryEntryStore(inner: AdaptiveProofOfDeliveryEntryStore, ttl: Int, clock: Clock): AdaptiveProofOfDeliveryEntryStore {
    return CachedAdaptiveProofOfDeliveryEntryStore(
        inner = inner,
        cache = newTtlCache<AdaptiveProofOfDeliveryEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive proof of delivery entries. Empty fields match all. */
data class AdaptiveProofOfDeliveryEntryQuery(
    var unit: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveProofOfDeliveryEntry): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveProofOfDeliveryEntry>): MutableList<AdaptiveProofOfDeliveryEntry> {
        val matched = filter(items) { candidate: AdaptiveProofOfDeliveryEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveProofOfDeliveryEntries(store: AdaptiveProofOfDeliveryEntryStore, query: AdaptiveProofOfDeliveryEntryQuery): MutableList<AdaptiveProofOfDeliveryEntry> {
    return query.apply(store.list())
}
