package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling proof of delivery entries by code. */
interface RollingProofOfDeliveryEntryStore {
    fun find(code: String): RollingProofOfDeliveryEntry?
    fun save(item: RollingProofOfDeliveryEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingProofOfDeliveryEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingProofOfDeliveryEntryStore(
    var items: MutableMap<String, RollingProofOfDeliveryEntry>,
    var order: MutableList<String>,
) : RollingProofOfDeliveryEntryStore {
    override fun find(code: String): RollingProofOfDeliveryEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingProofOfDeliveryEntry) {
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

    override fun list(): MutableList<RollingProofOfDeliveryEntry> {
        val out = mutableListOf<RollingProofOfDeliveryEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingProofOfDeliveryEntryStore(): RollingProofOfDeliveryEntryStore {
    return MemoryRollingProofOfDeliveryEntryStore(
        mutableMapOf<String, RollingProofOfDeliveryEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingProofOfDeliveryEntryStore(
    var inner: RollingProofOfDeliveryEntryStore,
    var cache: TtlCache<RollingProofOfDeliveryEntry>,
    var hits: Int,
    var misses: Int,
) : RollingProofOfDeliveryEntryStore {
    override fun find(code: String): RollingProofOfDeliveryEntry? {
        val hit = (cache.get(code) as RollingProofOfDeliveryEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingProofOfDeliveryEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingProofOfDeliveryEntry> = inner.list()
}

fun newCachedRollingProofOfDeliveryEntryStore(inner: RollingProofOfDeliveryEntryStore, ttl: Int, clock: Clock): RollingProofOfDeliveryEntryStore {
    return CachedRollingProofOfDeliveryEntryStore(
        inner = inner,
        cache = newTtlCache<RollingProofOfDeliveryEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling proof of delivery entries. Empty fields match all. */
data class RollingProofOfDeliveryEntryQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingProofOfDeliveryEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RollingProofOfDeliveryEntry>): MutableList<RollingProofOfDeliveryEntry> {
        val matched = filter(items) { candidate: RollingProofOfDeliveryEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingProofOfDeliveryEntries(store: RollingProofOfDeliveryEntryStore, query: RollingProofOfDeliveryEntryQuery): MutableList<RollingProofOfDeliveryEntry> {
    return query.apply(store.list())
}
