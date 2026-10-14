package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default accessorial rollups by code. */
interface DefaultAccessorialRollupStore {
    fun find(code: String): DefaultAccessorialRollup?
    fun save(item: DefaultAccessorialRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAccessorialRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAccessorialRollupStore(
    var items: MutableMap<String, DefaultAccessorialRollup>,
    var order: MutableList<String>,
) : DefaultAccessorialRollupStore {
    override fun find(code: String): DefaultAccessorialRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAccessorialRollup) {
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

    override fun list(): MutableList<DefaultAccessorialRollup> {
        val out = mutableListOf<DefaultAccessorialRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAccessorialRollupStore(): DefaultAccessorialRollupStore {
    return MemoryDefaultAccessorialRollupStore(
        mutableMapOf<String, DefaultAccessorialRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAccessorialRollupStore(
    var inner: DefaultAccessorialRollupStore,
    var cache: TtlCache<DefaultAccessorialRollup>,
    var hits: Int,
    var misses: Int,
) : DefaultAccessorialRollupStore {
    override fun find(code: String): DefaultAccessorialRollup? {
        val hit = (cache.get(code) as DefaultAccessorialRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAccessorialRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAccessorialRollup> = inner.list()
}

fun newCachedDefaultAccessorialRollupStore(inner: DefaultAccessorialRollupStore, ttl: Int, clock: Clock): DefaultAccessorialRollupStore {
    return CachedDefaultAccessorialRollupStore(
        inner = inner,
        cache = newTtlCache<DefaultAccessorialRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default accessorial rollups. Empty fields match all. */
data class DefaultAccessorialRollupQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAccessorialRollup): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAccessorialRollup>): MutableList<DefaultAccessorialRollup> {
        val matched = filter(items) { candidate: DefaultAccessorialRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAccessorialRollups(store: DefaultAccessorialRollupStore, query: DefaultAccessorialRollupQuery): MutableList<DefaultAccessorialRollup> {
    return query.apply(store.list())
}
