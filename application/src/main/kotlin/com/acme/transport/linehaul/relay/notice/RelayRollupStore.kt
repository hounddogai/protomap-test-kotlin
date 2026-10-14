package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay rollups by code. */
interface RelayRollupStore {
    fun find(code: String): RelayRollup?
    fun save(item: RelayRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayRollupStore(
    var items: MutableMap<String, RelayRollup>,
    var order: MutableList<String>,
) : RelayRollupStore {
    override fun find(code: String): RelayRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayRollup) {
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

    override fun list(): MutableList<RelayRollup> {
        val out = mutableListOf<RelayRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayRollupStore(): RelayRollupStore {
    return MemoryRelayRollupStore(mutableMapOf<String, RelayRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayRollupStore(
    var inner: RelayRollupStore,
    var cache: TtlCache<RelayRollup>,
    var hits: Int,
    var misses: Int,
) : RelayRollupStore {
    override fun find(code: String): RelayRollup? {
        val hit = (cache.get(code) as RelayRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayRollup> = inner.list()
}

fun newCachedRelayRollupStore(inner: RelayRollupStore, ttl: Int, clock: Clock): RelayRollupStore {
    return CachedRelayRollupStore(
        inner = inner,
        cache = newTtlCache<RelayRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay rollups. Empty fields match all. */
data class RelayRollupQuery(
    var locale: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayRollup): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayRollup>): MutableList<RelayRollup> {
        val matched = filter(items) { candidate: RelayRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayRollups(store: RelayRollupStore, query: RelayRollupQuery): MutableList<RelayRollup> {
    return query.apply(store.list())
}
