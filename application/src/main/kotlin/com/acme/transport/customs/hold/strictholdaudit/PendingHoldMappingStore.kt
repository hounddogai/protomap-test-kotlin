package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending hold mappings by code. */
interface PendingHoldMappingStore {
    fun find(code: String): PendingHoldMapping?
    fun save(item: PendingHoldMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingHoldMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingHoldMappingStore(
    var items: MutableMap<String, PendingHoldMapping>,
    var order: MutableList<String>,
) : PendingHoldMappingStore {
    override fun find(code: String): PendingHoldMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingHoldMapping) {
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

    override fun list(): MutableList<PendingHoldMapping> {
        val out = mutableListOf<PendingHoldMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingHoldMappingStore(): PendingHoldMappingStore {
    return MemoryPendingHoldMappingStore(mutableMapOf<String, PendingHoldMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingHoldMappingStore(
    var inner: PendingHoldMappingStore,
    var cache: TtlCache<PendingHoldMapping>,
    var hits: Int,
    var misses: Int,
) : PendingHoldMappingStore {
    override fun find(code: String): PendingHoldMapping? {
        val hit = (cache.get(code) as PendingHoldMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingHoldMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingHoldMapping> = inner.list()
}

fun newCachedPendingHoldMappingStore(inner: PendingHoldMappingStore, ttl: Int, clock: Clock): PendingHoldMappingStore {
    return CachedPendingHoldMappingStore(
        inner = inner,
        cache = newTtlCache<PendingHoldMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending hold mappings. Empty fields match all. */
data class PendingHoldMappingQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingHoldMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingHoldMapping>): MutableList<PendingHoldMapping> {
        val matched = filter(items) { candidate: PendingHoldMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingHoldMappings(store: PendingHoldMappingStore, query: PendingHoldMappingQuery): MutableList<PendingHoldMapping> {
    return query.apply(store.list())
}
