package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary accessorial overrides by code. */
interface PrimaryAccessorialOverrideStore {
    fun find(code: String): PrimaryAccessorialOverride?
    fun save(item: PrimaryAccessorialOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAccessorialOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAccessorialOverrideStore(
    var items: MutableMap<String, PrimaryAccessorialOverride>,
    var order: MutableList<String>,
) : PrimaryAccessorialOverrideStore {
    override fun find(code: String): PrimaryAccessorialOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAccessorialOverride) {
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

    override fun list(): MutableList<PrimaryAccessorialOverride> {
        val out = mutableListOf<PrimaryAccessorialOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAccessorialOverrideStore(): PrimaryAccessorialOverrideStore {
    return MemoryPrimaryAccessorialOverrideStore(
        mutableMapOf<String, PrimaryAccessorialOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAccessorialOverrideStore(
    var inner: PrimaryAccessorialOverrideStore,
    var cache: TtlCache<PrimaryAccessorialOverride>,
    var hits: Int,
    var misses: Int,
) : PrimaryAccessorialOverrideStore {
    override fun find(code: String): PrimaryAccessorialOverride? {
        val hit = (cache.get(code) as PrimaryAccessorialOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAccessorialOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAccessorialOverride> = inner.list()
}

fun newCachedPrimaryAccessorialOverrideStore(inner: PrimaryAccessorialOverrideStore, ttl: Int, clock: Clock): PrimaryAccessorialOverrideStore {
    return CachedPrimaryAccessorialOverrideStore(
        inner = inner,
        cache = newTtlCache<PrimaryAccessorialOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary accessorial overrides. Empty fields match all. */
data class PrimaryAccessorialOverrideQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAccessorialOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PrimaryAccessorialOverride>): MutableList<PrimaryAccessorialOverride> {
        val matched = filter(items) { candidate: PrimaryAccessorialOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAccessorialOverrides(store: PrimaryAccessorialOverrideStore, query: PrimaryAccessorialOverrideQuery): MutableList<PrimaryAccessorialOverride> {
    return query.apply(store.list())
}
