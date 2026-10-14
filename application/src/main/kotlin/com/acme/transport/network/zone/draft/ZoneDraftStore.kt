package com.acme.transport.network.zone.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone drafts by code. */
interface ZoneDraftStore {
    fun find(code: String): ZoneDraft?
    fun save(item: ZoneDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneDraftStore(
    var items: MutableMap<String, ZoneDraft>,
    var order: MutableList<String>,
) : ZoneDraftStore {
    override fun find(code: String): ZoneDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneDraft) {
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

    override fun list(): MutableList<ZoneDraft> {
        val out = mutableListOf<ZoneDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneDraftStore(): ZoneDraftStore {
    return MemoryZoneDraftStore(mutableMapOf<String, ZoneDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneDraftStore(
    var inner: ZoneDraftStore,
    var cache: TtlCache<ZoneDraft>,
    var hits: Int,
    var misses: Int,
) : ZoneDraftStore {
    override fun find(code: String): ZoneDraft? {
        val hit = (cache.get(code) as ZoneDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneDraft> = inner.list()
}

fun newCachedZoneDraftStore(inner: ZoneDraftStore, ttl: Int, clock: Clock): ZoneDraftStore {
    return CachedZoneDraftStore(
        inner = inner,
        cache = newTtlCache<ZoneDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone drafts. Empty fields match all. */
data class ZoneDraftQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneDraft>): MutableList<ZoneDraft> {
        val matched = filter(items) { candidate: ZoneDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneDrafts(store: ZoneDraftStore, query: ZoneDraftQuery): MutableList<ZoneDraft> {
    return query.apply(store.list())
}
