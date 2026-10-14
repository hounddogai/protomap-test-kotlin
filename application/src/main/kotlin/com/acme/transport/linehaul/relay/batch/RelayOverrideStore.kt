package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay overrides by code. */
interface RelayOverrideStore {
    fun find(code: String): RelayOverride?
    fun save(item: RelayOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayOverrideStore(
    var items: MutableMap<String, RelayOverride>,
    var order: MutableList<String>,
) : RelayOverrideStore {
    override fun find(code: String): RelayOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayOverride) {
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

    override fun list(): MutableList<RelayOverride> {
        val out = mutableListOf<RelayOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayOverrideStore(): RelayOverrideStore {
    return MemoryRelayOverrideStore(mutableMapOf<String, RelayOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayOverrideStore(
    var inner: RelayOverrideStore,
    var cache: TtlCache<RelayOverride>,
    var hits: Int,
    var misses: Int,
) : RelayOverrideStore {
    override fun find(code: String): RelayOverride? {
        val hit = (cache.get(code) as RelayOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayOverride> = inner.list()
}

fun newCachedRelayOverrideStore(inner: RelayOverrideStore, ttl: Int, clock: Clock): RelayOverrideStore {
    return CachedRelayOverrideStore(
        inner = inner,
        cache = newTtlCache<RelayOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay overrides. Empty fields match all. */
data class RelayOverrideQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayOverride>): MutableList<RelayOverride> {
        val matched = filter(items) { candidate: RelayOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayOverrides(store: RelayOverrideStore, query: RelayOverrideQuery): MutableList<RelayOverride> {
    return query.apply(store.list())
}
