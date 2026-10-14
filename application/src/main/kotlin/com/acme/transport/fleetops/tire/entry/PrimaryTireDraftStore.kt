package com.acme.transport.fleetops.tire.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary tire drafts by code. */
interface PrimaryTireDraftStore {
    fun find(code: String): PrimaryTireDraft?
    fun save(item: PrimaryTireDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTireDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTireDraftStore(
    var items: MutableMap<String, PrimaryTireDraft>,
    var order: MutableList<String>,
) : PrimaryTireDraftStore {
    override fun find(code: String): PrimaryTireDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTireDraft) {
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

    override fun list(): MutableList<PrimaryTireDraft> {
        val out = mutableListOf<PrimaryTireDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTireDraftStore(): PrimaryTireDraftStore {
    return MemoryPrimaryTireDraftStore(mutableMapOf<String, PrimaryTireDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTireDraftStore(
    var inner: PrimaryTireDraftStore,
    var cache: TtlCache<PrimaryTireDraft>,
    var hits: Int,
    var misses: Int,
) : PrimaryTireDraftStore {
    override fun find(code: String): PrimaryTireDraft? {
        val hit = (cache.get(code) as PrimaryTireDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTireDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTireDraft> = inner.list()
}

fun newCachedPrimaryTireDraftStore(inner: PrimaryTireDraftStore, ttl: Int, clock: Clock): PrimaryTireDraftStore {
    return CachedPrimaryTireDraftStore(
        inner = inner,
        cache = newTtlCache<PrimaryTireDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tire drafts. Empty fields match all. */
data class PrimaryTireDraftQuery(
    var category: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTireDraft): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTireDraft>): MutableList<PrimaryTireDraft> {
        val matched = filter(items) { candidate: PrimaryTireDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTireDrafts(store: PrimaryTireDraftStore, query: PrimaryTireDraftQuery): MutableList<PrimaryTireDraft> {
    return query.apply(store.list())
}
