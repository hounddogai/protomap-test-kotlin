package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global redelivery overrides by code. */
interface GlobalRedeliveryOverrideStore {
    fun find(code: String): GlobalRedeliveryOverride?
    fun save(item: GlobalRedeliveryOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRedeliveryOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRedeliveryOverrideStore(
    var items: MutableMap<String, GlobalRedeliveryOverride>,
    var order: MutableList<String>,
) : GlobalRedeliveryOverrideStore {
    override fun find(code: String): GlobalRedeliveryOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRedeliveryOverride) {
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

    override fun list(): MutableList<GlobalRedeliveryOverride> {
        val out = mutableListOf<GlobalRedeliveryOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRedeliveryOverrideStore(): GlobalRedeliveryOverrideStore {
    return MemoryGlobalRedeliveryOverrideStore(
        mutableMapOf<String, GlobalRedeliveryOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRedeliveryOverrideStore(
    var inner: GlobalRedeliveryOverrideStore,
    var cache: TtlCache<GlobalRedeliveryOverride>,
    var hits: Int,
    var misses: Int,
) : GlobalRedeliveryOverrideStore {
    override fun find(code: String): GlobalRedeliveryOverride? {
        val hit = (cache.get(code) as GlobalRedeliveryOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRedeliveryOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRedeliveryOverride> = inner.list()
}

fun newCachedGlobalRedeliveryOverrideStore(inner: GlobalRedeliveryOverrideStore, ttl: Int, clock: Clock): GlobalRedeliveryOverrideStore {
    return CachedGlobalRedeliveryOverrideStore(
        inner = inner,
        cache = newTtlCache<GlobalRedeliveryOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global redelivery overrides. Empty fields match all. */
data class GlobalRedeliveryOverrideQuery(
    var depot: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRedeliveryOverride): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRedeliveryOverride>): MutableList<GlobalRedeliveryOverride> {
        val matched = filter(items) { candidate: GlobalRedeliveryOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRedeliveryOverrides(store: GlobalRedeliveryOverrideStore, query: GlobalRedeliveryOverrideQuery): MutableList<GlobalRedeliveryOverride> {
    return query.apply(store.list())
}
