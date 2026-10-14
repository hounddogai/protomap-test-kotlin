package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient service area rollups by code. */
interface LenientServiceAreaRollupStore {
    fun find(code: String): LenientServiceAreaRollup?
    fun save(item: LenientServiceAreaRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientServiceAreaRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientServiceAreaRollupStore(
    var items: MutableMap<String, LenientServiceAreaRollup>,
    var order: MutableList<String>,
) : LenientServiceAreaRollupStore {
    override fun find(code: String): LenientServiceAreaRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientServiceAreaRollup) {
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

    override fun list(): MutableList<LenientServiceAreaRollup> {
        val out = mutableListOf<LenientServiceAreaRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientServiceAreaRollupStore(): LenientServiceAreaRollupStore {
    return MemoryLenientServiceAreaRollupStore(
        mutableMapOf<String, LenientServiceAreaRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientServiceAreaRollupStore(
    var inner: LenientServiceAreaRollupStore,
    var cache: TtlCache<LenientServiceAreaRollup>,
    var hits: Int,
    var misses: Int,
) : LenientServiceAreaRollupStore {
    override fun find(code: String): LenientServiceAreaRollup? {
        val hit = (cache.get(code) as LenientServiceAreaRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientServiceAreaRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientServiceAreaRollup> = inner.list()
}

fun newCachedLenientServiceAreaRollupStore(inner: LenientServiceAreaRollupStore, ttl: Int, clock: Clock): LenientServiceAreaRollupStore {
    return CachedLenientServiceAreaRollupStore(
        inner = inner,
        cache = newTtlCache<LenientServiceAreaRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient service area rollups. Empty fields match all. */
data class LenientServiceAreaRollupQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientServiceAreaRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientServiceAreaRollup>): MutableList<LenientServiceAreaRollup> {
        val matched = filter(items) { candidate: LenientServiceAreaRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientServiceAreaRollups(store: LenientServiceAreaRollupStore, query: LenientServiceAreaRollupQuery): MutableList<LenientServiceAreaRollup> {
    return query.apply(store.list())
}
