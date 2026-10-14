package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary redelivery overrides by code. */
interface PrimaryRedeliveryOverrideStore {
    fun find(code: String): PrimaryRedeliveryOverride?
    fun save(item: PrimaryRedeliveryOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRedeliveryOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRedeliveryOverrideStore(
    var items: MutableMap<String, PrimaryRedeliveryOverride>,
    var order: MutableList<String>,
) : PrimaryRedeliveryOverrideStore {
    override fun find(code: String): PrimaryRedeliveryOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRedeliveryOverride) {
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

    override fun list(): MutableList<PrimaryRedeliveryOverride> {
        val out = mutableListOf<PrimaryRedeliveryOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRedeliveryOverrideStore(): PrimaryRedeliveryOverrideStore {
    return MemoryPrimaryRedeliveryOverrideStore(
        mutableMapOf<String, PrimaryRedeliveryOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRedeliveryOverrideStore(
    var inner: PrimaryRedeliveryOverrideStore,
    var cache: TtlCache<PrimaryRedeliveryOverride>,
    var hits: Int,
    var misses: Int,
) : PrimaryRedeliveryOverrideStore {
    override fun find(code: String): PrimaryRedeliveryOverride? {
        val hit = (cache.get(code) as PrimaryRedeliveryOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRedeliveryOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRedeliveryOverride> = inner.list()
}

fun newCachedPrimaryRedeliveryOverrideStore(inner: PrimaryRedeliveryOverrideStore, ttl: Int, clock: Clock): PrimaryRedeliveryOverrideStore {
    return CachedPrimaryRedeliveryOverrideStore(
        inner = inner,
        cache = newTtlCache<PrimaryRedeliveryOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary redelivery overrides. Empty fields match all. */
data class PrimaryRedeliveryOverrideQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRedeliveryOverride): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryRedeliveryOverride>): MutableList<PrimaryRedeliveryOverride> {
        val matched = filter(items) { candidate: PrimaryRedeliveryOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRedeliveryOverrides(store: PrimaryRedeliveryOverrideStore, query: PrimaryRedeliveryOverrideQuery): MutableList<PrimaryRedeliveryOverride> {
    return query.apply(store.list())
}
