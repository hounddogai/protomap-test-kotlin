package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped idling targets by code. */
interface CappedIdlingTargetStore {
    fun find(code: String): CappedIdlingTarget?
    fun save(item: CappedIdlingTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedIdlingTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedIdlingTargetStore(
    var items: MutableMap<String, CappedIdlingTarget>,
    var order: MutableList<String>,
) : CappedIdlingTargetStore {
    override fun find(code: String): CappedIdlingTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedIdlingTarget) {
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

    override fun list(): MutableList<CappedIdlingTarget> {
        val out = mutableListOf<CappedIdlingTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedIdlingTargetStore(): CappedIdlingTargetStore {
    return MemoryCappedIdlingTargetStore(mutableMapOf<String, CappedIdlingTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedIdlingTargetStore(
    var inner: CappedIdlingTargetStore,
    var cache: TtlCache<CappedIdlingTarget>,
    var hits: Int,
    var misses: Int,
) : CappedIdlingTargetStore {
    override fun find(code: String): CappedIdlingTarget? {
        val hit = (cache.get(code) as CappedIdlingTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedIdlingTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedIdlingTarget> = inner.list()
}

fun newCachedCappedIdlingTargetStore(inner: CappedIdlingTargetStore, ttl: Int, clock: Clock): CappedIdlingTargetStore {
    return CachedCappedIdlingTargetStore(
        inner = inner,
        cache = newTtlCache<CappedIdlingTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped idling targets. Empty fields match all. */
data class CappedIdlingTargetQuery(
    var region: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedIdlingTarget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedIdlingTarget>): MutableList<CappedIdlingTarget> {
        val matched = filter(items) { candidate: CappedIdlingTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedIdlingTargets(store: CappedIdlingTargetStore, query: CappedIdlingTargetQuery): MutableList<CappedIdlingTarget> {
    return query.apply(store.list())
}
