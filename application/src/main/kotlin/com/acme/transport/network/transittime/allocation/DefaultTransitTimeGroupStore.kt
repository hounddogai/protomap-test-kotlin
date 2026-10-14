package com.acme.transport.network.transittime.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default transit time groups by code. */
interface DefaultTransitTimeGroupStore {
    fun find(code: String): DefaultTransitTimeGroup?
    fun save(item: DefaultTransitTimeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTransitTimeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTransitTimeGroupStore(
    var items: MutableMap<String, DefaultTransitTimeGroup>,
    var order: MutableList<String>,
) : DefaultTransitTimeGroupStore {
    override fun find(code: String): DefaultTransitTimeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTransitTimeGroup) {
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

    override fun list(): MutableList<DefaultTransitTimeGroup> {
        val out = mutableListOf<DefaultTransitTimeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTransitTimeGroupStore(): DefaultTransitTimeGroupStore {
    return MemoryDefaultTransitTimeGroupStore(
        mutableMapOf<String, DefaultTransitTimeGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTransitTimeGroupStore(
    var inner: DefaultTransitTimeGroupStore,
    var cache: TtlCache<DefaultTransitTimeGroup>,
    var hits: Int,
    var misses: Int,
) : DefaultTransitTimeGroupStore {
    override fun find(code: String): DefaultTransitTimeGroup? {
        val hit = (cache.get(code) as DefaultTransitTimeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTransitTimeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTransitTimeGroup> = inner.list()
}

fun newCachedDefaultTransitTimeGroupStore(inner: DefaultTransitTimeGroupStore, ttl: Int, clock: Clock): DefaultTransitTimeGroupStore {
    return CachedDefaultTransitTimeGroupStore(
        inner = inner,
        cache = newTtlCache<DefaultTransitTimeGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default transit time groups. Empty fields match all. */
data class DefaultTransitTimeGroupQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTransitTimeGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTransitTimeGroup>): MutableList<DefaultTransitTimeGroup> {
        val matched = filter(items) { candidate: DefaultTransitTimeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTransitTimeGroups(store: DefaultTransitTimeGroupStore, query: DefaultTransitTimeGroupQuery): MutableList<DefaultTransitTimeGroup> {
    return query.apply(store.list())
}
