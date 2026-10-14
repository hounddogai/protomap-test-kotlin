package com.acme.transport.fleetops.tire.tier

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy tire drafts by code. */
interface LegacyTireDraftStore {
    fun find(code: String): LegacyTireDraft?
    fun save(item: LegacyTireDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTireDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTireDraftStore(
    var items: MutableMap<String, LegacyTireDraft>,
    var order: MutableList<String>,
) : LegacyTireDraftStore {
    override fun find(code: String): LegacyTireDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTireDraft) {
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

    override fun list(): MutableList<LegacyTireDraft> {
        val out = mutableListOf<LegacyTireDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTireDraftStore(): LegacyTireDraftStore {
    return MemoryLegacyTireDraftStore(mutableMapOf<String, LegacyTireDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTireDraftStore(
    var inner: LegacyTireDraftStore,
    var cache: TtlCache<LegacyTireDraft>,
    var hits: Int,
    var misses: Int,
) : LegacyTireDraftStore {
    override fun find(code: String): LegacyTireDraft? {
        val hit = (cache.get(code) as LegacyTireDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTireDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTireDraft> = inner.list()
}

fun newCachedLegacyTireDraftStore(inner: LegacyTireDraftStore, ttl: Int, clock: Clock): LegacyTireDraftStore {
    return CachedLegacyTireDraftStore(
        inner = inner,
        cache = newTtlCache<LegacyTireDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tire drafts. Empty fields match all. */
data class LegacyTireDraftQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTireDraft): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<LegacyTireDraft>): MutableList<LegacyTireDraft> {
        val matched = filter(items) { candidate: LegacyTireDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTireDrafts(store: LegacyTireDraftStore, query: LegacyTireDraftQuery): MutableList<LegacyTireDraft> {
    return query.apply(store.list())
}
