package com.acme.transport.network.zone.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling zone drafts by code. */
interface RollingZoneDraftStore {
    fun find(code: String): RollingZoneDraft?
    fun save(item: RollingZoneDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingZoneDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingZoneDraftStore(
    var items: MutableMap<String, RollingZoneDraft>,
    var order: MutableList<String>,
) : RollingZoneDraftStore {
    override fun find(code: String): RollingZoneDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingZoneDraft) {
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

    override fun list(): MutableList<RollingZoneDraft> {
        val out = mutableListOf<RollingZoneDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingZoneDraftStore(): RollingZoneDraftStore {
    return MemoryRollingZoneDraftStore(mutableMapOf<String, RollingZoneDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingZoneDraftStore(
    var inner: RollingZoneDraftStore,
    var cache: TtlCache<RollingZoneDraft>,
    var hits: Int,
    var misses: Int,
) : RollingZoneDraftStore {
    override fun find(code: String): RollingZoneDraft? {
        val hit = (cache.get(code) as RollingZoneDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingZoneDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingZoneDraft> = inner.list()
}

fun newCachedRollingZoneDraftStore(inner: RollingZoneDraftStore, ttl: Int, clock: Clock): RollingZoneDraftStore {
    return CachedRollingZoneDraftStore(
        inner = inner,
        cache = newTtlCache<RollingZoneDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling zone drafts. Empty fields match all. */
data class RollingZoneDraftQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingZoneDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RollingZoneDraft>): MutableList<RollingZoneDraft> {
        val matched = filter(items) { candidate: RollingZoneDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingZoneDrafts(store: RollingZoneDraftStore, query: RollingZoneDraftQuery): MutableList<RollingZoneDraft> {
    return query.apply(store.list())
}
