package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient incident groups by code. */
interface LenientIncidentGroupStore {
    fun find(code: String): LenientIncidentGroup?
    fun save(item: LenientIncidentGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientIncidentGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientIncidentGroupStore(
    var items: MutableMap<String, LenientIncidentGroup>,
    var order: MutableList<String>,
) : LenientIncidentGroupStore {
    override fun find(code: String): LenientIncidentGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientIncidentGroup) {
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

    override fun list(): MutableList<LenientIncidentGroup> {
        val out = mutableListOf<LenientIncidentGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientIncidentGroupStore(): LenientIncidentGroupStore {
    return MemoryLenientIncidentGroupStore(
        mutableMapOf<String, LenientIncidentGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientIncidentGroupStore(
    var inner: LenientIncidentGroupStore,
    var cache: TtlCache<LenientIncidentGroup>,
    var hits: Int,
    var misses: Int,
) : LenientIncidentGroupStore {
    override fun find(code: String): LenientIncidentGroup? {
        val hit = (cache.get(code) as LenientIncidentGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientIncidentGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientIncidentGroup> = inner.list()
}

fun newCachedLenientIncidentGroupStore(inner: LenientIncidentGroupStore, ttl: Int, clock: Clock): LenientIncidentGroupStore {
    return CachedLenientIncidentGroupStore(
        inner = inner,
        cache = newTtlCache<LenientIncidentGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient incident groups. Empty fields match all. */
data class LenientIncidentGroupQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientIncidentGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientIncidentGroup>): MutableList<LenientIncidentGroup> {
        val matched = filter(items) { candidate: LenientIncidentGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientIncidentGroups(store: LenientIncidentGroupStore, query: LenientIncidentGroupQuery): MutableList<LenientIncidentGroup> {
    return query.apply(store.list())
}
