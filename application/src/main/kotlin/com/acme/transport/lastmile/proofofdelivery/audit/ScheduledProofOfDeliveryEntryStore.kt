package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled proof of delivery entries by code. */
interface ScheduledProofOfDeliveryEntryStore {
    fun find(code: String): ScheduledProofOfDeliveryEntry?
    fun save(item: ScheduledProofOfDeliveryEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledProofOfDeliveryEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledProofOfDeliveryEntryStore(
    var items: MutableMap<String, ScheduledProofOfDeliveryEntry>,
    var order: MutableList<String>,
) : ScheduledProofOfDeliveryEntryStore {
    override fun find(code: String): ScheduledProofOfDeliveryEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledProofOfDeliveryEntry) {
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

    override fun list(): MutableList<ScheduledProofOfDeliveryEntry> {
        val out = mutableListOf<ScheduledProofOfDeliveryEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledProofOfDeliveryEntryStore(): ScheduledProofOfDeliveryEntryStore {
    return MemoryScheduledProofOfDeliveryEntryStore(
        mutableMapOf<String, ScheduledProofOfDeliveryEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledProofOfDeliveryEntryStore(
    var inner: ScheduledProofOfDeliveryEntryStore,
    var cache: TtlCache<ScheduledProofOfDeliveryEntry>,
    var hits: Int,
    var misses: Int,
) : ScheduledProofOfDeliveryEntryStore {
    override fun find(code: String): ScheduledProofOfDeliveryEntry? {
        val hit = (cache.get(code) as ScheduledProofOfDeliveryEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledProofOfDeliveryEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledProofOfDeliveryEntry> = inner.list()
}

fun newCachedScheduledProofOfDeliveryEntryStore(inner: ScheduledProofOfDeliveryEntryStore, ttl: Int, clock: Clock): ScheduledProofOfDeliveryEntryStore {
    return CachedScheduledProofOfDeliveryEntryStore(
        inner = inner,
        cache = newTtlCache<ScheduledProofOfDeliveryEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled proof of delivery entries. Empty fields match all. */
data class ScheduledProofOfDeliveryEntryQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledProofOfDeliveryEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledProofOfDeliveryEntry>): MutableList<ScheduledProofOfDeliveryEntry> {
        val matched = filter(items) { candidate: ScheduledProofOfDeliveryEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledProofOfDeliveryEntries(store: ScheduledProofOfDeliveryEntryStore, query: ScheduledProofOfDeliveryEntryQuery): MutableList<ScheduledProofOfDeliveryEntry> {
    return query.apply(store.list())
}
