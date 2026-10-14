package com.acme.transport.customs.hold.recorditem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient hold groups by code. */
interface LenientHoldGroupStore {
    fun find(code: String): LenientHoldGroup?
    fun save(item: LenientHoldGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientHoldGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientHoldGroupStore(
    var items: MutableMap<String, LenientHoldGroup>,
    var order: MutableList<String>,
) : LenientHoldGroupStore {
    override fun find(code: String): LenientHoldGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientHoldGroup) {
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

    override fun list(): MutableList<LenientHoldGroup> {
        val out = mutableListOf<LenientHoldGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientHoldGroupStore(): LenientHoldGroupStore {
    return MemoryLenientHoldGroupStore(mutableMapOf<String, LenientHoldGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientHoldGroupStore(
    var inner: LenientHoldGroupStore,
    var cache: TtlCache<LenientHoldGroup>,
    var hits: Int,
    var misses: Int,
) : LenientHoldGroupStore {
    override fun find(code: String): LenientHoldGroup? {
        val hit = (cache.get(code) as LenientHoldGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientHoldGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientHoldGroup> = inner.list()
}

fun newCachedLenientHoldGroupStore(inner: LenientHoldGroupStore, ttl: Int, clock: Clock): LenientHoldGroupStore {
    return CachedLenientHoldGroupStore(
        inner = inner,
        cache = newTtlCache<LenientHoldGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient hold groups. Empty fields match all. */
data class LenientHoldGroupQuery(
    var country: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientHoldGroup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LenientHoldGroup>): MutableList<LenientHoldGroup> {
        val matched = filter(items) { candidate: LenientHoldGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientHoldGroups(store: LenientHoldGroupStore, query: LenientHoldGroupQuery): MutableList<LenientHoldGroup> {
    return query.apply(store.list())
}
