package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped rate card windows by code. */
interface CappedRateCardWindowStore {
    fun find(code: String): CappedRateCardWindow?
    fun save(item: CappedRateCardWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRateCardWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRateCardWindowStore(
    var items: MutableMap<String, CappedRateCardWindow>,
    var order: MutableList<String>,
) : CappedRateCardWindowStore {
    override fun find(code: String): CappedRateCardWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRateCardWindow) {
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

    override fun list(): MutableList<CappedRateCardWindow> {
        val out = mutableListOf<CappedRateCardWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRateCardWindowStore(): CappedRateCardWindowStore {
    return MemoryCappedRateCardWindowStore(
        mutableMapOf<String, CappedRateCardWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRateCardWindowStore(
    var inner: CappedRateCardWindowStore,
    var cache: TtlCache<CappedRateCardWindow>,
    var hits: Int,
    var misses: Int,
) : CappedRateCardWindowStore {
    override fun find(code: String): CappedRateCardWindow? {
        val hit = (cache.get(code) as CappedRateCardWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRateCardWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRateCardWindow> = inner.list()
}

fun newCachedCappedRateCardWindowStore(inner: CappedRateCardWindowStore, ttl: Int, clock: Clock): CappedRateCardWindowStore {
    return CachedCappedRateCardWindowStore(
        inner = inner,
        cache = newTtlCache<CappedRateCardWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped rate card windows. Empty fields match all. */
data class CappedRateCardWindowQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRateCardWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRateCardWindow>): MutableList<CappedRateCardWindow> {
        val matched = filter(items) { candidate: CappedRateCardWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRateCardWindows(store: CappedRateCardWindowStore, query: CappedRateCardWindowQuery): MutableList<CappedRateCardWindow> {
    return query.apply(store.list())
}
