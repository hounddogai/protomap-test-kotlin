package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default relay baselines by code. */
interface DefaultRelayBaselineStore {
    fun find(code: String): DefaultRelayBaseline?
    fun save(item: DefaultRelayBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRelayBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRelayBaselineStore(
    var items: MutableMap<String, DefaultRelayBaseline>,
    var order: MutableList<String>,
) : DefaultRelayBaselineStore {
    override fun find(code: String): DefaultRelayBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRelayBaseline) {
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

    override fun list(): MutableList<DefaultRelayBaseline> {
        val out = mutableListOf<DefaultRelayBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRelayBaselineStore(): DefaultRelayBaselineStore {
    return MemoryDefaultRelayBaselineStore(
        mutableMapOf<String, DefaultRelayBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRelayBaselineStore(
    var inner: DefaultRelayBaselineStore,
    var cache: TtlCache<DefaultRelayBaseline>,
    var hits: Int,
    var misses: Int,
) : DefaultRelayBaselineStore {
    override fun find(code: String): DefaultRelayBaseline? {
        val hit = (cache.get(code) as DefaultRelayBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRelayBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRelayBaseline> = inner.list()
}

fun newCachedDefaultRelayBaselineStore(inner: DefaultRelayBaselineStore, ttl: Int, clock: Clock): DefaultRelayBaselineStore {
    return CachedDefaultRelayBaselineStore(
        inner = inner,
        cache = newTtlCache<DefaultRelayBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default relay baselines. Empty fields match all. */
data class DefaultRelayBaselineQuery(
    var category: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRelayBaseline): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRelayBaseline>): MutableList<DefaultRelayBaseline> {
        val matched = filter(items) { candidate: DefaultRelayBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRelayBaselines(store: DefaultRelayBaselineStore, query: DefaultRelayBaselineQuery): MutableList<DefaultRelayBaseline> {
    return query.apply(store.list())
}
