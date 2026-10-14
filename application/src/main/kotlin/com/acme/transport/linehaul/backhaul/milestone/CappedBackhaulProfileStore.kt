package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped backhaul profiles by code. */
interface CappedBackhaulProfileStore {
    fun find(code: String): CappedBackhaulProfile?
    fun save(item: CappedBackhaulProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedBackhaulProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedBackhaulProfileStore(
    var items: MutableMap<String, CappedBackhaulProfile>,
    var order: MutableList<String>,
) : CappedBackhaulProfileStore {
    override fun find(code: String): CappedBackhaulProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedBackhaulProfile) {
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

    override fun list(): MutableList<CappedBackhaulProfile> {
        val out = mutableListOf<CappedBackhaulProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedBackhaulProfileStore(): CappedBackhaulProfileStore {
    return MemoryCappedBackhaulProfileStore(
        mutableMapOf<String, CappedBackhaulProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedBackhaulProfileStore(
    var inner: CappedBackhaulProfileStore,
    var cache: TtlCache<CappedBackhaulProfile>,
    var hits: Int,
    var misses: Int,
) : CappedBackhaulProfileStore {
    override fun find(code: String): CappedBackhaulProfile? {
        val hit = (cache.get(code) as CappedBackhaulProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedBackhaulProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedBackhaulProfile> = inner.list()
}

fun newCachedCappedBackhaulProfileStore(inner: CappedBackhaulProfileStore, ttl: Int, clock: Clock): CappedBackhaulProfileStore {
    return CachedCappedBackhaulProfileStore(
        inner = inner,
        cache = newTtlCache<CappedBackhaulProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped backhaul profiles. Empty fields match all. */
data class CappedBackhaulProfileQuery(
    var unit: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedBackhaulProfile): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedBackhaulProfile>): MutableList<CappedBackhaulProfile> {
        val matched = filter(items) { candidate: CappedBackhaulProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedBackhaulProfiles(store: CappedBackhaulProfileStore, query: CappedBackhaulProfileQuery): MutableList<CappedBackhaulProfile> {
    return query.apply(store.list())
}
