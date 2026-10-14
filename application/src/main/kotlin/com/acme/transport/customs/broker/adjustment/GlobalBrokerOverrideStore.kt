package com.acme.transport.customs.broker.adjustment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global broker overrides by code. */
interface GlobalBrokerOverrideStore {
    fun find(code: String): GlobalBrokerOverride?
    fun save(item: GlobalBrokerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalBrokerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalBrokerOverrideStore(
    var items: MutableMap<String, GlobalBrokerOverride>,
    var order: MutableList<String>,
) : GlobalBrokerOverrideStore {
    override fun find(code: String): GlobalBrokerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalBrokerOverride) {
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

    override fun list(): MutableList<GlobalBrokerOverride> {
        val out = mutableListOf<GlobalBrokerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalBrokerOverrideStore(): GlobalBrokerOverrideStore {
    return MemoryGlobalBrokerOverrideStore(
        mutableMapOf<String, GlobalBrokerOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalBrokerOverrideStore(
    var inner: GlobalBrokerOverrideStore,
    var cache: TtlCache<GlobalBrokerOverride>,
    var hits: Int,
    var misses: Int,
) : GlobalBrokerOverrideStore {
    override fun find(code: String): GlobalBrokerOverride? {
        val hit = (cache.get(code) as GlobalBrokerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalBrokerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalBrokerOverride> = inner.list()
}

fun newCachedGlobalBrokerOverrideStore(inner: GlobalBrokerOverrideStore, ttl: Int, clock: Clock): GlobalBrokerOverrideStore {
    return CachedGlobalBrokerOverrideStore(
        inner = inner,
        cache = newTtlCache<GlobalBrokerOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global broker overrides. Empty fields match all. */
data class GlobalBrokerOverrideQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalBrokerOverride): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<GlobalBrokerOverride>): MutableList<GlobalBrokerOverride> {
        val matched = filter(items) { candidate: GlobalBrokerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalBrokerOverrides(store: GlobalBrokerOverrideStore, query: GlobalBrokerOverrideQuery): MutableList<GlobalBrokerOverride> {
    return query.apply(store.list())
}
