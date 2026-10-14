package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery overrides by code. */
interface RedeliveryOverrideStore {
    fun find(code: String): RedeliveryOverride?
    fun save(item: RedeliveryOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryOverrideStore(
    var items: MutableMap<String, RedeliveryOverride>,
    var order: MutableList<String>,
) : RedeliveryOverrideStore {
    override fun find(code: String): RedeliveryOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryOverride) {
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

    override fun list(): MutableList<RedeliveryOverride> {
        val out = mutableListOf<RedeliveryOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryOverrideStore(): RedeliveryOverrideStore {
    return MemoryRedeliveryOverrideStore(mutableMapOf<String, RedeliveryOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryOverrideStore(
    var inner: RedeliveryOverrideStore,
    var cache: TtlCache<RedeliveryOverride>,
    var hits: Int,
    var misses: Int,
) : RedeliveryOverrideStore {
    override fun find(code: String): RedeliveryOverride? {
        val hit = (cache.get(code) as RedeliveryOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryOverride> = inner.list()
}

fun newCachedRedeliveryOverrideStore(inner: RedeliveryOverrideStore, ttl: Int, clock: Clock): RedeliveryOverrideStore {
    return CachedRedeliveryOverrideStore(
        inner = inner,
        cache = newTtlCache<RedeliveryOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery overrides. Empty fields match all. */
data class RedeliveryOverrideQuery(
    var source: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryOverride): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RedeliveryOverride>): MutableList<RedeliveryOverride> {
        val matched = filter(items) { candidate: RedeliveryOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryOverrides(store: RedeliveryOverrideStore, query: RedeliveryOverrideQuery): MutableList<RedeliveryOverride> {
    return query.apply(store.list())
}
