package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient locker overrides by code. */
interface LenientLockerOverrideStore {
    fun find(code: String): LenientLockerOverride?
    fun save(item: LenientLockerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientLockerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientLockerOverrideStore(
    var items: MutableMap<String, LenientLockerOverride>,
    var order: MutableList<String>,
) : LenientLockerOverrideStore {
    override fun find(code: String): LenientLockerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientLockerOverride) {
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

    override fun list(): MutableList<LenientLockerOverride> {
        val out = mutableListOf<LenientLockerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientLockerOverrideStore(): LenientLockerOverrideStore {
    return MemoryLenientLockerOverrideStore(
        mutableMapOf<String, LenientLockerOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientLockerOverrideStore(
    var inner: LenientLockerOverrideStore,
    var cache: TtlCache<LenientLockerOverride>,
    var hits: Int,
    var misses: Int,
) : LenientLockerOverrideStore {
    override fun find(code: String): LenientLockerOverride? {
        val hit = (cache.get(code) as LenientLockerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientLockerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientLockerOverride> = inner.list()
}

fun newCachedLenientLockerOverrideStore(inner: LenientLockerOverrideStore, ttl: Int, clock: Clock): LenientLockerOverrideStore {
    return CachedLenientLockerOverrideStore(
        inner = inner,
        cache = newTtlCache<LenientLockerOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient locker overrides. Empty fields match all. */
data class LenientLockerOverrideQuery(
    var unit: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientLockerOverride): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientLockerOverride>): MutableList<LenientLockerOverride> {
        val matched = filter(items) { candidate: LenientLockerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientLockerOverrides(store: LenientLockerOverrideStore, query: LenientLockerOverrideQuery): MutableList<LenientLockerOverride> {
    return query.apply(store.list())
}
