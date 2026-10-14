package com.acme.transport.customs.broker.group

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default broker overrides by code. */
interface DefaultBrokerOverrideStore {
    fun find(code: String): DefaultBrokerOverride?
    fun save(item: DefaultBrokerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultBrokerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultBrokerOverrideStore(
    var items: MutableMap<String, DefaultBrokerOverride>,
    var order: MutableList<String>,
) : DefaultBrokerOverrideStore {
    override fun find(code: String): DefaultBrokerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultBrokerOverride) {
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

    override fun list(): MutableList<DefaultBrokerOverride> {
        val out = mutableListOf<DefaultBrokerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultBrokerOverrideStore(): DefaultBrokerOverrideStore {
    return MemoryDefaultBrokerOverrideStore(
        mutableMapOf<String, DefaultBrokerOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultBrokerOverrideStore(
    var inner: DefaultBrokerOverrideStore,
    var cache: TtlCache<DefaultBrokerOverride>,
    var hits: Int,
    var misses: Int,
) : DefaultBrokerOverrideStore {
    override fun find(code: String): DefaultBrokerOverride? {
        val hit = (cache.get(code) as DefaultBrokerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultBrokerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultBrokerOverride> = inner.list()
}

fun newCachedDefaultBrokerOverrideStore(inner: DefaultBrokerOverrideStore, ttl: Int, clock: Clock): DefaultBrokerOverrideStore {
    return CachedDefaultBrokerOverrideStore(
        inner = inner,
        cache = newTtlCache<DefaultBrokerOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default broker overrides. Empty fields match all. */
data class DefaultBrokerOverrideQuery(
    var channel: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultBrokerOverride): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultBrokerOverride>): MutableList<DefaultBrokerOverride> {
        val matched = filter(items) { candidate: DefaultBrokerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultBrokerOverrides(store: DefaultBrokerOverrideStore, query: DefaultBrokerOverrideQuery): MutableList<DefaultBrokerOverride> {
    return query.apply(store.list())
}
