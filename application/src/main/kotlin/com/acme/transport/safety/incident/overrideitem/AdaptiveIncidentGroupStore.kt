package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive incident groups by code. */
interface AdaptiveIncidentGroupStore {
    fun find(code: String): AdaptiveIncidentGroup?
    fun save(item: AdaptiveIncidentGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveIncidentGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveIncidentGroupStore(
    var items: MutableMap<String, AdaptiveIncidentGroup>,
    var order: MutableList<String>,
) : AdaptiveIncidentGroupStore {
    override fun find(code: String): AdaptiveIncidentGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveIncidentGroup) {
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

    override fun list(): MutableList<AdaptiveIncidentGroup> {
        val out = mutableListOf<AdaptiveIncidentGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveIncidentGroupStore(): AdaptiveIncidentGroupStore {
    return MemoryAdaptiveIncidentGroupStore(
        mutableMapOf<String, AdaptiveIncidentGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveIncidentGroupStore(
    var inner: AdaptiveIncidentGroupStore,
    var cache: TtlCache<AdaptiveIncidentGroup>,
    var hits: Int,
    var misses: Int,
) : AdaptiveIncidentGroupStore {
    override fun find(code: String): AdaptiveIncidentGroup? {
        val hit = (cache.get(code) as AdaptiveIncidentGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveIncidentGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveIncidentGroup> = inner.list()
}

fun newCachedAdaptiveIncidentGroupStore(inner: AdaptiveIncidentGroupStore, ttl: Int, clock: Clock): AdaptiveIncidentGroupStore {
    return CachedAdaptiveIncidentGroupStore(
        inner = inner,
        cache = newTtlCache<AdaptiveIncidentGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive incident groups. Empty fields match all. */
data class AdaptiveIncidentGroupQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveIncidentGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveIncidentGroup>): MutableList<AdaptiveIncidentGroup> {
        val matched = filter(items) { candidate: AdaptiveIncidentGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveIncidentGroups(store: AdaptiveIncidentGroupStore, query: AdaptiveIncidentGroupQuery): MutableList<AdaptiveIncidentGroup> {
    return query.apply(store.list())
}
