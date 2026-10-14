package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time drafts by code. */
interface TransitTimeDraftStore {
    fun find(code: String): TransitTimeDraft?
    fun save(item: TransitTimeDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeDraftStore(
    var items: MutableMap<String, TransitTimeDraft>,
    var order: MutableList<String>,
) : TransitTimeDraftStore {
    override fun find(code: String): TransitTimeDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeDraft) {
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

    override fun list(): MutableList<TransitTimeDraft> {
        val out = mutableListOf<TransitTimeDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeDraftStore(): TransitTimeDraftStore {
    return MemoryTransitTimeDraftStore(mutableMapOf<String, TransitTimeDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeDraftStore(
    var inner: TransitTimeDraftStore,
    var cache: TtlCache<TransitTimeDraft>,
    var hits: Int,
    var misses: Int,
) : TransitTimeDraftStore {
    override fun find(code: String): TransitTimeDraft? {
        val hit = (cache.get(code) as TransitTimeDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeDraft> = inner.list()
}

fun newCachedTransitTimeDraftStore(inner: TransitTimeDraftStore, ttl: Int, clock: Clock): TransitTimeDraftStore {
    return CachedTransitTimeDraftStore(
        inner = inner,
        cache = newTtlCache<TransitTimeDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time drafts. Empty fields match all. */
data class TransitTimeDraftQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeDraft>): MutableList<TransitTimeDraft> {
        val matched = filter(items) { candidate: TransitTimeDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeDrafts(store: TransitTimeDraftStore, query: TransitTimeDraftQuery): MutableList<TransitTimeDraft> {
    return query.apply(store.list())
}
