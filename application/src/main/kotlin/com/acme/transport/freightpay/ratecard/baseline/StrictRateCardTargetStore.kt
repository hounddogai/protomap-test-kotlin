package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict rate card targets by code. */
interface StrictRateCardTargetStore {
    fun find(code: String): StrictRateCardTarget?
    fun save(item: StrictRateCardTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRateCardTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRateCardTargetStore(
    var items: MutableMap<String, StrictRateCardTarget>,
    var order: MutableList<String>,
) : StrictRateCardTargetStore {
    override fun find(code: String): StrictRateCardTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRateCardTarget) {
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

    override fun list(): MutableList<StrictRateCardTarget> {
        val out = mutableListOf<StrictRateCardTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRateCardTargetStore(): StrictRateCardTargetStore {
    return MemoryStrictRateCardTargetStore(
        mutableMapOf<String, StrictRateCardTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRateCardTargetStore(
    var inner: StrictRateCardTargetStore,
    var cache: TtlCache<StrictRateCardTarget>,
    var hits: Int,
    var misses: Int,
) : StrictRateCardTargetStore {
    override fun find(code: String): StrictRateCardTarget? {
        val hit = (cache.get(code) as StrictRateCardTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRateCardTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRateCardTarget> = inner.list()
}

fun newCachedStrictRateCardTargetStore(inner: StrictRateCardTargetStore, ttl: Int, clock: Clock): StrictRateCardTargetStore {
    return CachedStrictRateCardTargetStore(
        inner = inner,
        cache = newTtlCache<StrictRateCardTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict rate card targets. Empty fields match all. */
data class StrictRateCardTargetQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRateCardTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<StrictRateCardTarget>): MutableList<StrictRateCardTarget> {
        val matched = filter(items) { candidate: StrictRateCardTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRateCardTargets(store: StrictRateCardTargetStore, query: StrictRateCardTargetQuery): MutableList<StrictRateCardTarget> {
    return query.apply(store.list())
}
