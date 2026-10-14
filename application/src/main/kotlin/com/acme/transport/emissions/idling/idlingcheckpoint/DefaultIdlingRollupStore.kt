package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default idling rollups by code. */
interface DefaultIdlingRollupStore {
    fun find(code: String): DefaultIdlingRollup?
    fun save(item: DefaultIdlingRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultIdlingRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultIdlingRollupStore(
    var items: MutableMap<String, DefaultIdlingRollup>,
    var order: MutableList<String>,
) : DefaultIdlingRollupStore {
    override fun find(code: String): DefaultIdlingRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultIdlingRollup) {
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

    override fun list(): MutableList<DefaultIdlingRollup> {
        val out = mutableListOf<DefaultIdlingRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultIdlingRollupStore(): DefaultIdlingRollupStore {
    return MemoryDefaultIdlingRollupStore(mutableMapOf<String, DefaultIdlingRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultIdlingRollupStore(
    var inner: DefaultIdlingRollupStore,
    var cache: TtlCache<DefaultIdlingRollup>,
    var hits: Int,
    var misses: Int,
) : DefaultIdlingRollupStore {
    override fun find(code: String): DefaultIdlingRollup? {
        val hit = (cache.get(code) as DefaultIdlingRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultIdlingRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultIdlingRollup> = inner.list()
}

fun newCachedDefaultIdlingRollupStore(inner: DefaultIdlingRollupStore, ttl: Int, clock: Clock): DefaultIdlingRollupStore {
    return CachedDefaultIdlingRollupStore(
        inner = inner,
        cache = newTtlCache<DefaultIdlingRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default idling rollups. Empty fields match all. */
data class DefaultIdlingRollupQuery(
    var channel: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultIdlingRollup): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<DefaultIdlingRollup>): MutableList<DefaultIdlingRollup> {
        val matched = filter(items) { candidate: DefaultIdlingRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultIdlingRollups(store: DefaultIdlingRollupStore, query: DefaultIdlingRollupQuery): MutableList<DefaultIdlingRollup> {
    return query.apply(store.list())
}
